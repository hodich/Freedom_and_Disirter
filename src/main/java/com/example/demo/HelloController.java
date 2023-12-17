package com.example.demo;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HelloController {
    private static final Logger logger = LogManager.getLogger();


    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button buildHome;

    @FXML
    private Button collectWater;

    @FXML
    private Button exploreNewTerritory;

    @FXML
    private Button waterPlants;

    // Добавить метки для отображения данных игрока
    @FXML
    private Text dayLabel;
    @FXML
    private Label waterLabel;
    @FXML
    private Label riceLabel;
    @FXML
    private Label peasantsLabel;
    @FXML
    private Label housesLabel;

    // Добавить метки для отображения данных AI
    @FXML
    private Label A_waterLabel;
    @FXML
    private Label A_riceLabel;
    @FXML
    private Label A_peasantsLabel;
    @FXML
    private Label A_housesLabel;

    // Значения для владельца ячейки на карте
    private static final int NEUTRAL = 0;
    private static final int PLAYER = 1;
    private static final int AI = 2;

    // Переменная для игрового поля размером 10x10
    private final int MAP_SIZE = 10;
    private int[][] gameMap = new int[MAP_SIZE][MAP_SIZE];

    // Переменные для хранения данных игрока

    private int day = 0;
    private int P_Cell = 10;
    private int P_Cell_Opened = 1;
    private int P_chars = 1;
    private int P_houses = 1;
    private int P_plants = 1;
    private int P_water = 1;
    private boolean Can_Pchar_Claim = true;
    private boolean Can_PBuild_Home = true;

    // Переменные для хранения данных AI
    private int A_Cell = 10;
    private int A_Cell_Opened = 1;
    private int A_chars = 1;
    private int A_houses = 1;
    private int A_plants = 1;
    private int A_water = 1;
    private boolean Can_Achar_Claim = true;
    private boolean Can_ABuild_Home = true;

    // Константы для игровой логики
    private static boolean end = false;
    private static final int Chars_4_Calim_Cell = 5;
    private static final int Plants_Grows = 1;
    private static final int Chars_From_House = 1;

    private void initGameMap() {
        for (int[] row : gameMap) {
            Arrays.fill(row, NEUTRAL);
        }
    }

    // Метод для обновления данных игрока и AI, а также элементов GUI
    private void update() {

        logger.info("Запускается обновление данных");

        if (!end) {
            // Проверка может ли игрок захватить новую территорию

            Can_Pchar_Claim = P_chars >= Chars_4_Calim_Cell;

            // Проверка возможности постройки дома

            Can_PBuild_Home = P_chars >= Chars_From_House && P_plants >= 1 && P_water >= 1;

            // Проверка может ли AI захватить новую территорию

            Can_Achar_Claim = A_chars >= Chars_4_Calim_Cell;

            // Проверка возможности постройки дома AI

            Can_ABuild_Home = A_chars >= Chars_From_House && A_plants >= 1 && A_water >= 1;

            // Обновление меток с данными игрока

            dayLabel.setText("День " + day + "-й");
            waterLabel.setText("Воды: " + P_water);
            riceLabel.setText("Риса: " + P_plants);
            peasantsLabel.setText("Крестьян: " + P_chars);
            housesLabel.setText("Домов: " + P_houses);

            // Обновление меток с данными AI

            A_waterLabel.setText("Воды: " + A_water);
            A_riceLabel.setText("Риса: " + A_plants);
            A_peasantsLabel.setText("Крестьян: " + A_chars);
            A_housesLabel.setText("Домов: " + A_houses);

            // Обновление доступности кнопок в зависимости от возможности действия игрока

            collectWater.setDisable(false); // Кнопка для набора воды всегда доступна
            waterPlants.setDisable(false); // Кнопка для полива риса всегда доступна
            exploreNewTerritory.setDisable(!Can_Pchar_Claim); // Кнопка для исследования новой территории доступна, если есть достаточно крестьян
            buildHome.setDisable(!Can_PBuild_Home); // Кнопка для постройки дома доступна, если есть достаточно ресурсов
        }
    }

    // Метод для обработки нажатия на кнопку для набора воды игроком

    /**
     * @param event
     */
    @FXML
    void collectWater(ActionEvent event) {

        logger.info("Запускает сбор воды");

        P_water++; // Увеличить количество воды игрока
        update(); // Обновить данные и GUI
        performAITurn();
        day++;
    }

    /**
     * @param event //указывает на то, что метод принимает в качестве параметра объект event
     */
    // Метод для обработки нажатия на кнопку для полива риса игроком
    @FXML
    void waterPlants(ActionEvent event) {

        logger.info("Запускает поливание риса");

        if (P_water > 0) {
            P_water--;
            P_plants += Plants_Grows;// Увеличить количество рисa игрока
            update(); // Обновить данные и GUI
            performAITurn();
            day++;
        }
    }

    /**
     * @param event //указывает на то, что метод принимает в качестве параметра объект event
     */
    // Метод для обработки нажатия на кнопку для исследования новой территории игроком
    @FXML
    void exploreNewTerritory(ActionEvent event) {

        logger.info("Запускает исследование новых территорий");

        if (Can_Pchar_Claim) { // Проверить, есть ли достаточно крестьян у игрока
            P_Cell--; // Уменьшить количество оставшихся территорий
            P_Cell_Opened++;
            claimTerritory(PLAYER); // Захват новой территории игроком
            checkAndDeclareWinner(); // Проверка условий победы
            update(); // Обновить данные и GUI
            performAITurn();
            day++;
        }
    }

    /**
     * @param event //указывает на то, что метод принимает в качестве параметра объект event
     */
    // Метод для обработки нажатия на кнопку для постройки дома игроком
    @FXML
    void buildHome(ActionEvent event) {

        logger.info("Запускает постройку дома");

        if (Can_PBuild_Home) { // Проверить, есть ли достаточно ресурсов у игрока
            P_houses++; // Увеличить количество домов игрока на 1
            P_chars += Chars_From_House; // Увеличить количество крестьян игрока
            P_plants--; // Уменьшить количество риса игрока
            P_water--; // Уменьшить количество воды игрока
            update(); // Обновить данные и GUI
            performAITurn(); // Ход AI
            day++; // Увеличение количества дней
        }
    }

    private void performAITurn() {

        logger.info("Запускает очередность ходов AI");

        if (Can_Achar_Claim) {
            exploreNewTerritoryAI(); // Изучение новых территорий
        } else if (Can_ABuild_Home) {
            buildHomeAI(); // Строит дом
        } else if (A_water > 0) {
            waterPlantsAI(); // Поливает рис
        } else {
            collectWaterAI(); // Собирает воду
        }
        update(); // Обновление GUI и данных
    }

    // Метод для обработки нажатия на кнопку для исследования новой территории AI
    private void exploreNewTerritoryAI() {

        logger.info("Запускает исследование новых территорий AI");

        if (Can_Achar_Claim) { // Проверить, есть ли достаточно крестьян у AI
            A_Cell--; // Уменьшить количество оставшихся территорий AI
            A_Cell_Opened++;
            claimTerritory(AI); // Захват новой территории AI
            checkAndDeclareWinner();// Проверка условий победы
            update(); // Обновить данные и GUI
        }
    }

    // Метод для обработки нажатия на кнопку для полива риса AI
    private void waterPlantsAI() {

        logger.info("Запускает поливание риса AI");

        if (A_water > 0) { // Проверить, есть ои достаточное количество воды у AI
            A_water--; // Уменьшение количества оставшейся воды у AI
            A_plants += Plants_Grows; // Увеличить количество риса AI
            update(); // Обновить данные и GUI
        }
    }

    // Метод для обработки нажатия на кнопку для постройки дома AI
    private void buildHomeAI() {

        logger.info("Запускает постройку дома AI");

        if (Can_ABuild_Home) { // Проверить, есть ли достаточно ресурсов у AI
            A_houses++; // Увеличить количество домов AI на 1
            A_chars += Chars_From_House; // Увеличить количество крестьян AI
            A_plants--; // Уменьшить количество риса AI
            A_water--; // Уменьшить количество воды AI
            update(); // Обновить данные и GUI
        }
    }

    private void collectWaterAI() {

        logger.info("Запускает сбор воды AI");

        A_water++; // Увеличить количество воды AI
        update(); // Обновить данные и GUI
    }

    /**
     * @param player // Номер игрока, для которого производится проверка
     * @return //Возвращает true, если игрок занял более половины всех ячеек на карте; иначе false.
     */
    private boolean checkWinCondition(int player) {

        logger.info("Запускает проверку выйгрыша");

        int ownedCells = 0;
        for (int[] row : gameMap) {
            for (int cell : row) {
                if (cell == player) {
                    ownedCells++;
                }
            }
        }
        return ownedCells >= (MAP_SIZE * MAP_SIZE) / 2;
    }

    // Метод для обработки захвата новой территории игроком или AI

    /**
     * @param player // Номер игрока, для которого производится проверка
     */
    private void claimTerritory(int player) {

        logger.info("Запускает проверку захваченных территорий");

        for (int i = 0; i < MAP_SIZE; i++) {
            for (int j = 0; j < MAP_SIZE; j++) {
                if (gameMap[i][j] == NEUTRAL) {
                    gameMap[i][j] = player;
                    return; // Закончить после захвата первой свободной ячейки
                }
            }
        }
    }

    // Проверка победителя и объявление его
    private void checkAndDeclareWinner() {

        logger.info("Запускает проверку и объявление победителя");

        if (checkWinCondition(PLAYER)) {
            // Игрок выигрывает
            // Обработка победы игрока
            dayLabel.setText("Игорок победил");
            end = true;
        } else if (checkWinCondition(AI)) {
            // AI выигрывает
            // Обработка победы AI
            dayLabel.setText("AI победил");
            end = true;
        }

    }
}

