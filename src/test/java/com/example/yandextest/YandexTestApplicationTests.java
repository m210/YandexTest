package com.example.yandextest;

import com.example.yandextest.controller.MarketController;
import com.example.yandextest.model.*;
import com.example.yandextest.repository.ImportRequestRepository;
import com.example.yandextest.repository.ShopUnitImportRepository;
import com.example.yandextest.service.MarketServiceImp;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.assertj.core.api.Assertions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = MarketController.class)
class YandexTestApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImportRequestRepository requestRepository;

    @MockBean
    private ShopUnitImportRepository shopUnitImportRepository;

    @SpyBean
    private MarketServiceImp marketService;

    private static final String ROOT_ID = "069cb8d7-bbdd-47d3-ad8f-82ef4c269df1";
    private static final ShopUnitImport ROOT = new ShopUnitImport();
    private static final ShopUnitImport SMARTPHONES = new ShopUnitImport();
    private static final ShopUnitImport JPHONE13 = new ShopUnitImport();
    private static final ShopUnitImport XOMIAREADME10 = new ShopUnitImport();
    private static final ShopUnitImport TVS = new ShopUnitImport();
    private static final ShopUnitImport SAMSON70 = new ShopUnitImport();
    private static final ShopUnitImport PHYLLIS50 = new ShopUnitImport();
    private static final ShopUnitImport GOLDSTAR65 = new ShopUnitImport();

    private static final ShopUnitImportRequest[] BATCHES = new ShopUnitImportRequest[5];

    static {
        ROOT.setName("Товары");
        ROOT.setId(ROOT_ID);
        ROOT.setType(ShopUnitType.CATEGORY);

        SMARTPHONES.setName("Смартфоны");
        SMARTPHONES.setId("d515e43f-f3f6-4471-bb77-6b455017a2d2");
        SMARTPHONES.setParentId(ROOT_ID);
        SMARTPHONES.setType(ShopUnitType.CATEGORY);

        JPHONE13.setName("jPhone 13");
        JPHONE13.setId("863e1a7a-1304-42ae-943b-179184c077e3");
        JPHONE13.setParentId(SMARTPHONES.getId());
        JPHONE13.setType(ShopUnitType.OFFER);
        JPHONE13.setPrice(79999L);

        XOMIAREADME10.setName("Xomiа Readme 10");
        XOMIAREADME10.setId("b1d8fd7d-2ae3-47d5-b2f9-0f094af800d4");
        XOMIAREADME10.setParentId(SMARTPHONES.getId());
        XOMIAREADME10.setType(ShopUnitType.OFFER);
        XOMIAREADME10.setPrice(59999L);

        TVS.setName("Телевизоры");
        TVS.setId("1cc0129a-2bfe-474c-9ee6-d435bf5fc8f2");
        TVS.setParentId(ROOT_ID);
        TVS.setType(ShopUnitType.CATEGORY);

        SAMSON70.setName("Samson 70\\\" LED UHD Smart");
        SAMSON70.setId("98883e8f-0507-482f-bce2-2fb306cf6483");
        SAMSON70.setParentId(TVS.getId());
        SAMSON70.setType(ShopUnitType.OFFER);
        SAMSON70.setPrice(32999L);

        PHYLLIS50.setName("Phyllis 50\\\" LED UHD Smarter");
        PHYLLIS50.setId("74b81fda-9cdc-4b63-8927-c978afed5cf4");
        PHYLLIS50.setParentId(TVS.getId());
        PHYLLIS50.setType(ShopUnitType.OFFER);
        PHYLLIS50.setPrice(49999L);

        GOLDSTAR65.setName("Goldstar 65\\\" LED UHD LOL Very Smart");
        GOLDSTAR65.setId("73bc3b36-02d1-4245-ab35-3106c9ee1c65");
        GOLDSTAR65.setParentId(TVS.getId());
        GOLDSTAR65.setType(ShopUnitType.OFFER);
        GOLDSTAR65.setPrice(69999L);

        int i = 0;
        BATCHES[i] = new ShopUnitImportRequest();
        BATCHES[i].setUpdateDate(LocalDateTime.parse("2022-02-01T12:00:00"));
        BATCHES[i++].setItems(List.of(ROOT));

        BATCHES[i] = new ShopUnitImportRequest();
        BATCHES[i].setUpdateDate(LocalDateTime.parse("2022-02-02T12:00:00"));
        BATCHES[i++].setItems(List.of(SMARTPHONES, JPHONE13, XOMIAREADME10));

        BATCHES[i] = new ShopUnitImportRequest();
        BATCHES[i].setUpdateDate(LocalDateTime.parse("2022-02-03T12:00:00"));
        BATCHES[i++].setItems(List.of(TVS, SAMSON70, PHYLLIS50));

        BATCHES[i] = new ShopUnitImportRequest();
        BATCHES[i].setUpdateDate(LocalDateTime.parse("2022-02-03T15:00:00"));
        BATCHES[i].setItems(List.of(GOLDSTAR65));
    }

    @Test
    public void shouldReturnSuccessImport() throws Exception {
        ShopUnitImportRequest batch = BATCHES[0];

        JSONArray array = new JSONArray();
        array.add(ROOT);
        JSONObject object = new JSONObject();
        object.put("updateDate", batch.getUpdateDate().toString());
        object.put("items", array);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/imports")
                        .content(object.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("The insert or update was successful."));

        verify(requestRepository, atLeastOnce()).save(batch);
        verify(shopUnitImportRepository, atLeastOnce()).save(ROOT);
    }

    @Test
    public void shouldReturnImportBadRequestIfDateNotISO8601() throws Exception {
        JSONObject object = new JSONObject();
        object.put("updateDate", LocalDateTime.now().atZone(ZoneId.of("UTC")).format(DateTimeFormatter.RFC_1123_DATE_TIME));
        object.put("items", new JSONArray());

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/imports")
                        .content(object.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnNotFoundDelete() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/delete/{id}", ROOT_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Item not found"));
    }

    @Test
    public void shouldReturnSuccessDelete() throws Exception {
        ShopUnitImportRequest batch = BATCHES[0];

        when(shopUnitImportRepository.findById(any(String.class))).thenReturn(Optional.of(ROOT));
        when(requestRepository.findAll()).thenReturn(List.of(batch));

        mockMvc.perform(MockMvcRequestBuilders.delete("/delete/{id}", ROOT_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("The removal was successful."));

        verify(shopUnitImportRepository, atLeastOnce()).delete(ROOT);
        verify(requestRepository, atLeastOnce()).delete(batch);
    }

    @Test
    public void shouldReturnExpectedDateAndPrice() throws Exception {
        when(requestRepository.findAll()).thenReturn(List.of(BATCHES[0], BATCHES[1], BATCHES[2], BATCHES[3]));

        String expectedDate = ZonedDateTime.of(BATCHES[3].getUpdateDate(), ZoneId.of("UTC"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
        long expectedPrice = (JPHONE13.getPrice()
                + XOMIAREADME10.getPrice()
                + SAMSON70.getPrice()
                + PHYLLIS50.getPrice()
                + GOLDSTAR65.getPrice()) / 5;

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/nodes/{id}", ROOT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(Long.toString(expectedPrice)))
                .andExpect(jsonPath("$.date").value(expectedDate));
    }

    @Test
    public void shouldReturnStatusOk() throws Exception {
        when(requestRepository.findAll()).thenReturn(List.of(BATCHES[0], BATCHES[1], BATCHES[2], BATCHES[3]));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/node/{id}/statistic", ROOT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnAllShopUnits() {
        when(requestRepository.findAll()).thenReturn(List.of(BATCHES[0], BATCHES[1], BATCHES[2], BATCHES[3]));

        ShopUnitStatisticResponse response = marketService.statistics(ROOT_ID, null, null);

        Assertions.assertThat(response.getShopUnitImports().size()).isEqualTo(8);
    }

    @Test
    public void shouldReturnExpectedResponse() {
        when(requestRepository.findAll()).thenReturn(List.of(BATCHES[0], BATCHES[1], BATCHES[2], BATCHES[3]));

        ShopUnitStatisticResponse response = marketService.statistics(ROOT_ID, "2022-02-03T11:00:00", "2022-02-03T15:00:00");

        Assertions.assertThat(response.getShopUnitImports().size()).isEqualTo(3); //TVs (1 category + 2 offers)

        ZonedDateTime expectedDate = ZonedDateTime.of(BATCHES[2].getUpdateDate(), ZoneId.of("UTC"));
        long expectedPrice = (SAMSON70.getPrice() + PHYLLIS50.getPrice()) / 2;

        for(ShopUnitStatisticUnit unit : response.getShopUnitImports()) {
            if(unit.getType() == ShopUnitType.CATEGORY) {
                Assertions.assertThat(unit.getPrice()).isEqualTo(expectedPrice);
                Assertions.assertThat(unit.getDate()).isEqualTo(expectedDate);
            }
        }
    }

    @Test
    public void shouldReturnNullWithoutCrash() {
        Assertions.assertThat(marketService.findNode(null)).isNull();
        Assertions.assertThat(marketService.sales(null)).isNull();
    }

    @Test
    public void shouldReturnEmptyListWithoutCrash() {
        ShopUnitStatisticResponse result = marketService.statistics(null, null, null);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getShopUnitImports()).isEqualTo(new ArrayList<ShopUnitStatisticUnit>());
    }

    @Test
    public void shouldReturn404WithoutCrash() {
        Assertions.assertThat(marketService.deleteNode(null)).isEqualTo(404);
    }

    @Test
    public void shouldReturn400WithoutCrash() {
        ShopUnitImportRequest batch = new ShopUnitImportRequest();

        when(shopUnitImportRepository.findById(any(String.class))).thenReturn(Optional.of(ROOT));
        when(requestRepository.findAll()).thenReturn(List.of(batch));

        Assertions.assertThat(marketService.deleteNode(ROOT_ID)).isEqualTo(400);
    }

    @Test
    public void shouldReturnFalseWithoutCrash() {
        Assertions.assertThat(marketService.importBatch(null)).isEqualTo(false);
    }
}
