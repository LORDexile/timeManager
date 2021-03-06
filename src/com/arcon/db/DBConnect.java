package com.arcon.db;

import com.arcon.ui.model.*;
import com.arcon.lib.Constants;

import javax.swing.*;
import java.util.*;
import java.sql.*;
import java.util.Date;

public class DBConnect{
    private static DBConnect instance = new DBConnect();
    private Connection connection = null;
    private Statement statement = null;
    private ResultSet resSet;

    private String url;
    private String driver;

    public DBConnect() {
        url = Constants.DB_URL;
        driver = Constants.DB_DRIVER;
    }

    public void openConnect() {

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try{
            connection = DriverManager.getConnection(url);
            statement = connection.createStatement();
            System.out.println("Соеденение установлено:");
        }catch (SQLException e){
            e.getStackTrace();
        }
    }

    public void closeConnect() {

        if (resSet != null) {
            try {
                resSet.close();
            }catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Соединение завершено");
    }

    /**
     *
     * @param userName
     * @param password
     * @return
     * 2 - verification passed;
     * <p>
     * 1 - password is`t correct;
     * </p>
     * 0 - User does`t exist.
     *
     */
    public int verifyUser (String userName, String password){
        try {
            resSet = statement.executeQuery("SELECT * FROM Users");
            while (resSet.next()) {
                if (userName.equals(resSet.getString("UserName"))){
                    if (password.equals(resSet.getString("Password"))) {
                        Constants.setUserName(resSet.getString("UserName"));
                        Constants.setUserType(resSet.getString("UserType"));
                        return 2;
                    }else {
                        return 1;
                    }

                }
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void createNewUser (User user){

        boolean NotExist = true;
        try {
            resSet = statement.executeQuery("SELECT * FROM Users");
            while (resSet.next()) {
                if (user.getUserName().equals(resSet.getString("UserName"))) {
                    NotExist = false;
                    break;
                }
            }

            if (NotExist) {

                String sql = "INSERT INTO Users (UserName,UserType,Password,Comment) " +
                        "VALUES ('" + user.getUserName() +
                        "', '" + user.getUserType() +
                        "', '" + user.getPassword() +
                        "', '" + user.getComment() +
                        "');";
                System.out.println(sql);
                statement.executeUpdate(sql);


            } else System.out.println("login exist");

        }catch (SQLException e){
            e.printStackTrace();
        }

    }

    public boolean isCardInUse (String id) {

        try {
            resSet = statement.executeQuery("SELECT * FROM CardInUse");

            while (resSet.next()) {
                if (id.equals(resSet.getString("id"))) {
                    return true;
                }
            }

        }catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public void writeNewCardInUse(String id) {
        try {
            String sql = "INSERT INTO CardInUse (id,EnterTime) " +
                        "VALUES ('" + id +
                        "', '" + new Date().getTime() +
                        "');";

            statement.executeUpdate(sql);

        }catch (SQLException e){
            e.printStackTrace();
        }
        setCardCount(1);
    }

    public void writeCard(Card card) {
        try{
            String sql = "INSERT INTO Cards (Card_id, User, Price, Discount, DATA_in, DATA_out) " +
                    "VALUES ('" + card.getId() +
                    "', '" + Constants.getUserName() +
                    "', '" + card.getPrice() +
                    "', '" + card.getDiscount() +
                    "', '" + card.getEnterTime() +
                    "', '" + card.getExitTime() +
                    "');";
            System.out.println(sql);
            statement.executeUpdate(sql);
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteCardInUse(String id) {
        try {
            String sql = "DELETE FROM CardInUse WHERE id=" + id + ";";
            statement.executeUpdate(sql);
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void deleteUser(User user) {
        try {
            String sql = "DELETE FROM Users WHERE UserName='" + user.getUserName() + "';";

            statement.executeUpdate(sql);

        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void deleteDiscount(Discount discount){
        try {
            String sql = "DELETE FROM Discount WHERE Discount=" + discount.getDiscount() +
                    " AND UserType='" + discount.getUserType() +
                    "';";

            statement.executeUpdate(sql);

        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void switchDiscount(Discount discount) {
        int isActive;
        if(discount.isActive()){
            isActive = 0;
        }else {
            isActive = 1;
        }

        try {
            String sql = "UPDATE Discount set Active =" + isActive + " WHERE Discount=" + discount.getDiscount() +
                    " AND UserType='" + discount.getUserType() +
                    "';";

            statement.executeUpdate(sql);

        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void setTransaction(int money, ActionType actionType) {
        setTransaction(money, actionType, null);
    }

    public void setTransaction(int money, ActionType actionType, String comment) {
        if (actionType.equals(ActionType.CARD_OUTPUT)) {
            setCardCount(-1);
        }
        setMoneyCount(money);

        try{
            String sql = "INSERT INTO money_transactions (number_of_cash, action, user_name, date, comment) " +
                    "VALUES ('" + money +
                    "', '" + actionType.toString() +
                    "', '" + Constants.getUserName() +
                    "', '" + new Date() +
                    "', '" + comment +
                    "');";

            statement.executeUpdate(sql);

        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setCardCount(int cardAction) {
        int cardCount = getCardCount();

        switch (cardAction){
            case 1:
                cardCount += 1;
                break;
            case -1:
                cardCount -= 1;
                break;
            default:
                cardCount += 0;
                break;
        }

        String sql = "UPDATE items set count = " + cardCount + " where id = 'card';";
        try {
            statement.executeUpdate(sql);
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void setMoneyCount(int money) {
        money += getMoneyCount();

        String sql = "UPDATE items set count = " + money + " WHERE id = 'money';";
        try {
            statement.executeUpdate(sql);
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void setGlobalPrice(double price, String password){
        if (verifyUser(Constants.getUserName(), password) == 2){

            String sql = "UPDATE constants set value_real = " + price + " WHERE `key` = 'price_per_hour';";

            try {
                statement.executeUpdate(sql);
            }catch (SQLException e){
                e.printStackTrace();
            }

        } else {
            JOptionPane.showMessageDialog(null,"Password incorrect!", "Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    public void setDiscount(double discount, String comment, UserType userType){
        try{
            String sql = "INSERT INTO Discount (Discount, Comment, UserType, Active) " +
                    "VALUES ('" + discount +
                    "', '" + comment +
                    "', '" + userType +
                    "', '" + 1 +
                    "');";

            System.out.println(sql);
            statement.executeUpdate(sql);

        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public double getGlobalPrice() {
        try {
            resSet = statement.executeQuery("SELECT * FROM constants WHERE `key` = 'price_per_hour';");
            return resSet.getDouble("value_real");
        }catch (SQLException e){
            e.printStackTrace();
        }
        return 0.0;
    }

    public int getMoneyCount() {
        try {
            resSet = statement.executeQuery("SELECT * FROM items WHERE id = 'money';");
            return resSet.getInt("count");
        }catch (SQLException e){
            e.printStackTrace();
        }
        return -999999;
    }

    public int getCardCount() {
        try {
            resSet = statement.executeQuery("SELECT * FROM items WHERE id = 'card';");
            return resSet.getInt("count");
        }catch (SQLException e){
            e.printStackTrace();
        }
        return -999999;
    }

    public Card getCardInUseById(String id) {
        Date date = null;
        try{
            resSet = statement.executeQuery("SELECT * FROM CardInUse");
            while (resSet.next()) {
                if (id.equals(resSet.getString("id"))) {
                    date = new Date(resSet.getLong("EnterTime"));
                }
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return new Card(Long.parseLong(id), date);
    }

    public List<Card> getCardInUseList(){
        List<Card> list = new ArrayList<>();
        Card card;
        try{
            resSet = statement.executeQuery("SELECT * FROM CardInUse");

            while (resSet.next()) {
                card = new Card(resSet.getLong("id"), new Date(resSet.getLong("EnterTime")));
                list.add(0,card);
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Transaction> getTransactionList() {
        List<Transaction> list = new ArrayList<>();
        Transaction transaction;
        try{
            resSet = statement.executeQuery("SELECT * FROM money_transactions");

            while (resSet.next()) {
                transaction = new Transaction(resSet.getInt("number_of_cash"), resSet.getString("action"),
                        resSet.getString("user_name"), resSet.getString("date"), resSet.getString("comment"));
                list.add(0,transaction);
            }

        }catch (SQLException e){
            e.printStackTrace();
        }
        return list;
    }

    public List<Discount> getDiscountList() {
        List<Discount> list = new ArrayList<>();

        try {
            resSet = statement.executeQuery("SELECT * FROM Discount");
            while (resSet.next()) {
                    list.add(new Discount(resSet.getDouble("Discount"), resSet.getString("Comment"), resSet.getString("UserType"), resSet.getInt("Active")));
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Discount> getUserDiscountList() {
        List<Discount> list = new ArrayList<>();

        try {
            resSet = statement.executeQuery("SELECT * FROM Discount");
            while (resSet.next()) {
                if ((resSet.getString("UserType").equals(Constants.getUserType()) || Constants.getUserType().equals(UserType.ADMIN.toString())) && resSet.getInt("Active")==1) {
                    list.add(new Discount(resSet.getDouble("Discount"), resSet.getString("Comment"), resSet.getString("UserType"), resSet.getInt("Active")));
                }
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Card> getCardsList() {
        List<Card> list = new ArrayList<>();
        Card card;
        try {
            resSet = statement.executeQuery("SELECT * FROM Cards");
            while (resSet.next()) {
                card = new Card(resSet.getInt("Operation_id"),
                        resSet.getInt("Card_id"),
                        resSet.getString("User"),
                        resSet.getInt("Price"),
                        resSet.getDouble("Discount"),
                        resSet.getString("DATA_in"),
                        resSet.getString("DATA_out")
                        );
                list.add(0, card);
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<User> getUsersList() {
        List<User> list = new ArrayList<>();
        User user;
        try {
            resSet = statement.executeQuery("SELECT * FROM Users");
            while (resSet.next()) {
                user = new User(resSet.getString("UserName"),
                        UserType.toUserType(resSet.getString("UserType")),
                        null,
                        resSet.getString("Comment")
                );
                list.add(user);
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static DBConnect getInstance() {
        return instance;
    }
}
