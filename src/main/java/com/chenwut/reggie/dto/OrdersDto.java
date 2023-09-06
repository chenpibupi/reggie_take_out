package com.chenwut.reggie.dto;

import com.chenwut.reggie.entity.OrderDetail;
import com.chenwut.reggie.entity.Orders;
import lombok.Data;
import java.util.List;

@Data
public class OrdersDto extends Orders {

    private String userName;

    private String phone;

    private String address;

    private String consignee;

    private List<OrderDetail> orderDetails;
	
}
