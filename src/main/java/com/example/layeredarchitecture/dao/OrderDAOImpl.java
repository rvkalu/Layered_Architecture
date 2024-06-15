package com.example.layeredarchitecture.dao;

import com.example.layeredarchitecture.db.DBConnection;
import com.example.layeredarchitecture.model.OrderDTO;
import com.example.layeredarchitecture.model.OrderDetailDTO;

import java.sql.*;
import java.util.List;

public class OrderDAOImpl  implements  OrderDAO {
    private Connection connection = DBConnection.getDbConnection().getConnection();
    OrderDetailsDAO orderDetailsDAO = new OrderDetailsDAOImpl();
    public OrderDAOImpl() throws SQLException, ClassNotFoundException {
    }

    @Override
    public String lastOrderId() throws SQLException, ClassNotFoundException {

        Statement stm = connection.createStatement();
        ResultSet rst = stm.executeQuery("SELECT oid FROM `Orders` ORDER BY oid DESC LIMIT 1;");

        return rst.next() ? String.format("OID-%03d", (Integer.parseInt(rst.getString("oid").replace("OID-", "")) + 1)) : "OID-001";


    }

    @Override
    public boolean checkOrderIdExist(String orderId) throws SQLException, ClassNotFoundException {

        PreparedStatement stm = connection.prepareStatement("SELECT oid FROM `Orders` WHERE oid=?");
        stm.setString(1, orderId);
        return stm.executeQuery().next();
    }

    @Override
    public boolean saveOrder(OrderDTO orderDTO, List<OrderDetailDTO> orderDetails) throws SQLException, ClassNotFoundException {
        connection.setAutoCommit(false);
        PreparedStatement stm = connection.prepareStatement("INSERT INTO `Orders` (oid, date, customerID) VALUES (?,?,?)");
        stm.setString(1, orderDTO.getOrderId());
        stm.setDate(2, Date.valueOf(orderDTO.getOrderDate()));
        stm.setString(3, orderDTO.getCustomerId());


        if (stm.executeUpdate() == 1) {

            if (orderDetailsDAO.addOrderDetails(orderDTO.getOrderId(), orderDetails)) {

                connection.commit();
                connection.setAutoCommit(true);
                return true;

            }

        connection.rollback();
        connection.setAutoCommit(true);


        }connection.rollback();
        connection.setAutoCommit(true);
        return false;
    }


}
