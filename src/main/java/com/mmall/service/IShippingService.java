package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping;

/**
 * Created by NoNo_Ni on 2020/4/10.
 */
public interface IShippingService {
    ServerResponse add(Integer userId , Shipping shipping);
    ServerResponse del(Integer userId, Integer shippingId);
    ServerResponse update(Integer userId , Shipping shipping);
    ServerResponse<Shipping> select(Integer userId , Integer shippingId);
    ServerResponse<PageInfo> list(Integer userId, int pageNum, int pageSize);
}
