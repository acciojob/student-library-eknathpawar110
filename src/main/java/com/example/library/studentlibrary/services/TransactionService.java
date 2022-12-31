package com.example.library.studentlibrary.services;

import com.example.library.studentlibrary.models.*;
import com.example.library.studentlibrary.repositories.BookRepository;
import com.example.library.studentlibrary.repositories.CardRepository;
import com.example.library.studentlibrary.repositories.TransactionRepository;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class TransactionService {

    @Autowired
    BookRepository bookRepository5;

    @Autowired
    CardRepository cardRepository5;

    @Autowired
    TransactionRepository transactionRepository5;

    @Value("${books.max_allowed}")
    int max_allowed_books;

    @Value("${books.max_allowed_days}")
    int getMax_allowed_days;

    @Value("${books.fine.per_day}")
    int fine_per_day;

    public String issueBook(int cardId, int bookId) throws Exception {
        //check whether bookId and cardId already exist
        //conditions required for successful transaction of issue book:
        //1. book is present and available
        // If it fails: throw new Exception("Book is either unavailable or not present");
        //2. card is present and activated
        // If it fails: throw new Exception("Card is invalid");
        //3. number of books issued against the card is strictly less than max_allowed_books
        // If it fails: throw new Exception("Book limit has reached for this card");
        //If the transaction is successful, save the transaction to the list of transactions and return the id

        List<Book> availableBooks = bookRepository5.findByAvailability(true);
        List<Card> availableCards = cardRepository5.findAll();
        boolean cardExist = cardRepository5.existsById(cardId);
        boolean bookExist = bookRepository5.existsById(bookId);

        boolean bookAvailable = false;
        Book orderedBook = null;
        if(bookExist == true) {
            for (Book x : availableBooks) {
                if (x.getId() == bookId) {
                    bookAvailable = true;
                    orderedBook = x;
                }
            }
        }

        boolean cardAvailabile = false;
        Card orderingCard = null;
        if(cardExist == true) {
            for(Card x: availableCards) {
                if(x.getCardStatus() == CardStatus.ACTIVATED) {
                    cardAvailabile = true;
                    orderingCard = x;
                }
            }
        }

        //1. condition book unavailable
        if(bookAvailable == false)
            throw new Exception("Book is either unavailable or not present");

        if(cardAvailabile == false)
            throw new Exception("Card is invalid");

        List<Book> booksIssued = orderingCard.getBooks();

        if(booksIssued.size() >= max_allowed_books)
            throw new Exception("Book limit has reached for this card");


        orderedBook.setAvailable(false);
        bookRepository5.updateBook(orderedBook);
        booksIssued.add(orderedBook);
        orderingCard.setBooks(booksIssued);
        cardRepository5.save(orderingCard);

        Transaction t = new Transaction();
        t.setBook(orderedBook);
        t.setCard(orderingCard);
        t.setTransactionStatus(TransactionStatus.SUCCESSFUL);
        t.setIssueOperation(true);

        transactionRepository5.save(t);
        //Note that the error message should match exactly in all cases

        return t.getTransactionId(); //return transactionId instead
    }

    public Transaction returnBook(int cardId, int bookId) throws Exception{

        List<Transaction> transactions = transactionRepository5.find(cardId, bookId,TransactionStatus.SUCCESSFUL, true);
        Transaction transaction = transactions.get(transactions.size() - 1);

        //for the given transaction calculate the fine amount considering the book has been returned exactly when this function is called
        //make the book available for other users
        //make a new transaction for return book which contains the fine amount as well

        Transaction returnBookTransaction = new Transaction();

        int fineAmount = transaction.getFineAmount();


        Date presentDate = returnBookTransaction.getTransactionDate();
        Date issuedDate = transaction.getTransactionDate();
        int diff = presentDate.compareTo(issuedDate);

        if(diff > 15) {
            fineAmount += 5;
        }

        boolean bookExist = bookRepository5.existsById(bookId);
        List<Book> availableBooks = bookRepository5.findByAvailability(false);

        Book returnedBook = null;
        for(Book x: availableBooks) {
            if(x.getId() == bookId) {
                returnedBook = x;
            }
        }
        returnedBook.setAvailable(true);
        bookRepository5.updateBook(returnedBook);



        returnBookTransaction.setFineAmount(fineAmount);
        returnBookTransaction.setTransactionStatus(TransactionStatus.SUCCESSFUL);
        returnBookTransaction.setIssueOperation(false);
        return returnBookTransaction; //return the transaction after updating all details
    }
}