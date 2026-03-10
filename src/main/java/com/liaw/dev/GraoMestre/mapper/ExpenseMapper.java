package com.liaw.dev.GraoMestre.mapper;

import com.liaw.dev.GraoMestre.dto.request.ExpeneRequestDTO;
import com.liaw.dev.GraoMestre.dto.response.ExpenseResponseDTO;
import com.liaw.dev.GraoMestre.entity.Expense;
import org.springframework.stereotype.Component;

@Component
public class ExpenseMapper {

    public ExpenseResponseDTO toResponse(Expense expense){
        return new ExpenseResponseDTO(
                expense.getId(),
                expense.getName(),
                expense.getPrice(),
                expense.getDate()
        );
    }

    public Expense toEntity (ExpeneRequestDTO request){
        Expense expense = new Expense();
        expense.setName(request.getName());
        expense.setPrice(request.getPrice());
        return expense;
    }

}
