package com.liaw.dev.GraoMestre.mapper;

import com.liaw.dev.GraoMestre.dto.request.ExpeneRequest;
import com.liaw.dev.GraoMestre.dto.response.ExpenseResponse;
import com.liaw.dev.GraoMestre.entity.Expense;
import org.springframework.stereotype.Component;

@Component
public class ExpenseMapper {

    public ExpenseResponse toResponse(Expense expense){
        return new ExpenseResponse(
                expense.getId(),
                expense.getName(),
                expense.getPrice(),
                expense.getDate()
        );
    }

    public Expense toEntity (ExpeneRequest request){
        Expense expense = new Expense();
        expense.setName(request.getName());
        expense.setPrice(request.getPrice());
        return expense;
    }

}
