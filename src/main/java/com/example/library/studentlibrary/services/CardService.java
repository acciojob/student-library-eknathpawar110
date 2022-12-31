package com.example.library.studentlibrary.services;

import com.example.library.studentlibrary.models.Card;
import com.example.library.studentlibrary.models.CardStatus;
import com.example.library.studentlibrary.models.Student;
import com.example.library.studentlibrary.repositories.CardRepository;
import com.example.library.studentlibrary.repositories.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CardService {

    @Autowired
    StudentRepository studentRepository;
    @Autowired
    CardRepository cardRepository3;

    public Card createAndReturn(Student student){

        Card card = null;
        card.setStudent(student);
        cardRepository3.save(card);
        Student newStudent = student;
        newStudent.setCard(card);
        studentRepository.updateStudentDetails(newStudent);
        //link student with a new card
        return card;
    }

    public void deactivateCard(int student_id){
        cardRepository3.deactivateCard(student_id, CardStatus.DEACTIVATED.toString());
    }
}