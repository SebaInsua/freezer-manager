package com.freezermanager.service;

import org.springframework.stereotype.Service;

import com.freezermanager.domain.food.Food;
import com.freezermanager.domain.storage.StorageExit;
import com.freezermanager.domain.storage.StorageExitDetail;
import com.freezermanager.dto.StorageExitDetailRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StorageExitDetailService {
    public StorageExitDetail buildExitDetail(Food food,StorageExitDetailRequest detail,StorageExit exit){
        StorageExitDetail newDetail = new StorageExitDetail();
        newDetail.setFood(food);
        newDetail.setCantidad(detail.cantidad());
        newDetail.setSelectedUnit(detail.selectedUnit());
        newDetail.setStorageExit(exit);
        return newDetail;
    }
}
