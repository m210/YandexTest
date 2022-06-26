package com.example.yandextest.service;

import com.example.yandextest.model.*;
import com.example.yandextest.repository.ShopUnitImportRepository;
import com.example.yandextest.repository.ImportRequestRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class MarketServiceImp implements MarketService {

    private final ImportRequestRepository importRequestRepository;
    private final ShopUnitImportRepository itemRepository;

    public MarketServiceImp(ImportRequestRepository importRequestRepository, ShopUnitImportRepository itemRepository) {
        this.importRequestRepository = importRequestRepository;
        this.itemRepository = itemRepository;
    }

    /**
     * Импортирует новые товары и/или категории. Товары/категории импортированные повторно обновляют текущие. Изменение типа элемента с товара на категорию или с категории на товар не допускается. Порядок элементов в запросе является произвольным.
     *
     * - uuid товара или категории является уникальным среди товаров и категорий
     * - родителем товара или категории может быть только категория
     * - принадлежность к категории определяется полем parentId
     * - товар или категория могут не иметь родителя (при обновлении parentId на null, элемент остается без родителя)
     * - название элемента не может быть null
     * - у категорий поле price должно содержать null
     * - цена товара не может быть null и должна быть больше либо равна нулю.
     * - при обновлении товара/категории обновленными считаются **все** их параметры
     * - при обновлении параметров элемента обязательно обновляется поле **date** в соответствии с временем обновления
     * - в одном запросе не может быть двух элементов с одинаковым id
     * - дата должна обрабатываться согласно ISO 8601 (такой придерживается OpenAPI). Если дата не удовлетворяет данному формату,
     * необходимо отвечать 400.
     *
     * Гарантируется, что во входных данных нет циклических зависимостей и поле updateDate монотонно возрастает. Гарантируется, что при проверке передаваемое время кратно секундам.
     *
     * @param batch
     */
    @Override
    public boolean importBatch(ShopUnitImportRequest batch) {
        try {
            DateTimeFormatter.ISO_DATE_TIME.parse(batch.getUpdateDate().toString());
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }

        for (ShopUnitImport item : batch.getItems()) {
            if(item.getType() == ShopUnitType.CATEGORY && item.getPrice() != null) {
                return false;
            }
            if(item.getType() == ShopUnitType.OFFER && (item.getPrice() == null || item.getPrice() < 0)) {
                return false;
            }
        }

        importRequestRepository.save(batch);
        for (ShopUnitImport item : batch.getItems()) {
            itemRepository.save(item);
        }

        return true;
    }

    /**
     * Удалить элемент по идентификатору.
     * При удалении категории удаляются все дочерние элементы.
     * Доступ к статистике (истории обновлений) удаленного элемента невозможен.
     *
     * Так как время удаления не передается, при удалении элемента время обновления родителя изменять не нужно.
     *
     * @param id
     */
    @Override
    public int deleteNode(String id) {
        if(!itemRepository.findById(id).isPresent()) {
            return 404;
        }

        try {
            for (ShopUnitImportRequest batch : importRequestRepository.findAll()) {
                List<ShopUnitImport> items = batch.getItems();
                if (items.size() == 0) {
                    continue;
                }

                for (ShopUnitImport item : items) {
                    if (item.getId().equals(id)) {
                        itemRepository.delete(item);
                        List<ShopUnit> nodes = findChildren(id);
                        for (ShopUnit node : nodes) {
                            deleteNode(node.getId());
                        }
                    }
                }

                if (itemRepository.countAllByParent(batch) == 0) {
                    importRequestRepository.delete(batch);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 400;
        }

        return 200;
    }

    /**
     * Получить информацию об элементе по идентификатору.
     * При получении информации о категории также предоставляется информация о её дочерних элементах.
     *
     * - для пустой категории поле children равно пустому массиву, а для товара равно null
     * - цена категории - это средняя цена всех её товаров, включая товары дочерних категорий.
     * Если категория не содержит товаров цена равна null.
     * При обновлении цены товара, средняя цена категории, которая содержит этот товар, тоже обновляется.
     *
     * @param id
     */

    @Override
    public ShopUnit findNode(String id) {
        ShopUnit tree = buildShopUnitTree(id);
        if(tree != null) {
            calcAveragePrice(tree);
        }
        return tree;
    }

    /**
     * Получение списка **товаров**,
     * цена которых была обновлена за последние 24 часа включительно
     * [now() - 24h, now()]
     * от времени переданном в запросе.
     * Обновление цены не означает её изменение.
     * Обновления цен удаленных товаров недоступны.
     * При обновлении цены товара, средняя цена категории, которая содержит этот товар,
     * тоже обновляется.
     *
     * @param date
     */

    @Override
    public ShopUnitStatisticResponse sales(String date) {
        try {
            LocalDateTime toTime = LocalDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME);
            LocalDateTime fromTime = toTime.minusDays(1);

            ShopUnitStatisticResponse response = new ShopUnitStatisticResponse();
            List<ShopUnitStatisticUnit> offers = new ArrayList<>();

            for (ShopUnitImportRequest batch : importRequestRepository.findAll()) {
                LocalDateTime updateTime = batch.getUpdateDate();
                if (updateTime.isAfter(fromTime) && updateTime.isBefore(toTime)) {
                    for (ShopUnitImport item : batch.getItems()) {
                        if (item.getType() == ShopUnitType.OFFER) {
                            ShopUnitStatisticUnit unit = new ShopUnitStatisticUnit();

                            unit.setId(item.getId());
                            unit.setName(item.getName());
                            unit.setParentId(item.getParentId());
                            unit.setType(item.getType());
                            unit.setPrice(item.getPrice());
                            unit.setDate(ZonedDateTime.of(batch.getUpdateDate(), ZoneId.of("UTC")));

                            offers.add(unit);
                        }
                    }
                }
            }

            if (offers.size() > 0) {
                response.setShopUnitImports(offers);
                return response;
            }
        } catch(RuntimeException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Получение статистики (истории обновлений)
     * по товару/категории за заданный полуинтервал [from, to).
     * Статистика по удаленным элементам недоступна.
     *
     * - цена категории - это средняя цена всех её товаров, включая товары дочерних категорий.
     *  Если категория не содержит товаров цена равна null.
     *  При обновлении цены товара, средняя цена категории, которая содержит этот товар, тоже обновляется.
     * - можно получить статистику за всё время.
     *
     * @param id
     * @param dateStart
     * @param dateEnd
     */
    @Override
    public ShopUnitStatisticResponse statistics(String id, String dateStart, String dateEnd) {
        ZonedDateTime fromTime = null;
        ZonedDateTime toTime = null;
        if(dateStart != null) {
            fromTime = ZonedDateTime.of(LocalDateTime.parse(dateStart, DateTimeFormatter.ISO_DATE_TIME) , ZoneId.of("UTC"));
        }
        if(dateEnd != null) {
            toTime = ZonedDateTime.of(LocalDateTime.parse(dateEnd, DateTimeFormatter.ISO_DATE_TIME) , ZoneId.of("UTC"));
        }

        ShopUnit tree = buildShopUnitTree(id);
        List<ShopUnitStatisticUnit> result = new ArrayList<>();
        for(ShopUnit category : getCategories(tree)) {
            Iterator<ShopUnit> it = category.getChildren().iterator();
            if(isDateInRange(category.getDate(), fromTime, toTime)) {
                result.add(category);
            }

            while(it.hasNext()) {
                ShopUnit child = it.next();
                if(!isDateInRange(child.getDate(), fromTime, toTime)) {
                    it.remove();
                } else if(child.getType() == ShopUnitType.OFFER) {
                    result.add(child);
                }
            }
        }

        for(ShopUnitStatisticUnit unit : result) {
            if(unit.getType() == ShopUnitType.CATEGORY) {
                calcAveragePrice((ShopUnit) unit);
            }
        }
        ShopUnitStatisticResponse response = new ShopUnitStatisticResponse();
        response.setShopUnitImports(result);

        return response;
    }

    public List<ShopUnit> getCategories(ShopUnit node) {
        List<ShopUnit> result = new ArrayList<>();
        if(node != null) {
            if (node.getType() == ShopUnitType.CATEGORY) {
                result.add(node);
            }

            for (ShopUnit child : node.getChildren()) {
                if (child.getType() == ShopUnitType.CATEGORY) {
                    result.addAll(getCategories(child));
                }
            }
        }
        return result;
    }

    private boolean isDateInRange(ZonedDateTime date, ZonedDateTime dateStart, ZonedDateTime dateEnd) {
        if(dateStart == null && dateEnd == null) {
            return true;
        }

        if(dateEnd != null && dateStart == null) {
            return date.isBefore(dateEnd);
        } else if(dateEnd == null) {
            return date.isAfter(dateStart);
        }

        return date.isAfter(dateStart) && date.isBefore(dateEnd);
    }

    private List<ShopUnit> findChildren(String id) {
        List<ShopUnit> children = new ArrayList<>();

        for(ShopUnitImportRequest batch : importRequestRepository.findAll()) {
            for(ShopUnitImport item : batch.getItems()) {
                String parent = item.getParentId();
                if(parent != null && parent.equals(id)) {
                    children.add(buildShopUnitTree(item.getId()));
                }
            }
        }

        return children;
    }

    private ShopUnit buildShopUnitTree(String id) {
        for(ShopUnitImportRequest batch : importRequestRepository.findAll()) {
            for(ShopUnitImport item : batch.getItems()) {
                if(item.getId().equals(id)) {
                    ShopUnit node = new ShopUnit();
                    node.setType(item.getType());
                    node.setId(id);
                    node.setName(item.getName());
                    node.setParentId(item.getParentId());
                    List<ShopUnit> children = findChildren(item.getId());
                    if(item.getType() == ShopUnitType.OFFER && children.size() == 0) {
                        children = null;
                    }
                    node.setChildren(children);
                    node.setDate(ZonedDateTime.of(batch.getUpdateDate(), ZoneId.of("UTC")));
                    node.setPrice(item.getPrice());

                    return node;
                }
            }
        }
        return null;
    }

    private void calcAveragePrice(ShopUnit node) {
        long price = 0;
        int count = 0;
        ZonedDateTime date = null;
        for(ShopUnit category : getCategories(node)) {
            for(ShopUnit child : category.getChildren()) {
                if (child.getType() == ShopUnitType.OFFER) {
                    if(date == null || date.isBefore(child.getDate())) {
                        date = child.getDate();
                    }
                    price += child.getPrice();
                    count++;
                } else {
                    calcAveragePrice(child);
                }
            }
        }
        if(count != 0) {
            node.setPrice(price / count);
        } else {
            node.setPrice(null);
        }
        node.setDate(date);
    }
}
