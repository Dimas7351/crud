package org.example.dao;

import org.example.models.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.net.SocketTimeoutException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class PersonDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PersonDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public List<Person> index(){

        return  jdbcTemplate.query("SELECT * FROM Person", new BeanPropertyRowMapper<>(Person.class));//можно не писать собственный RawMapper
    }

    public Optional<Person> show(String email){
        return jdbcTemplate.query("Select * from Person where email=?", new Object[] {email},
                new BeanPropertyRowMapper<>(Person.class)).stream().findAny();
    }

    public Person show(int id){
       return jdbcTemplate.query("SELECT * FROM Person where id=?",new Object[]{id},new PersonMapper())
               .stream().findAny().orElse(null);
    }

    public void save(Person person){
        jdbcTemplate.update("insert into person(name,age,email,address) values(?,?,?,?)",person.getName(),person.getAge()
        ,person.getEmail(), person.getAddress());
    }

    public void update(int id, Person updatedPerson){

        jdbcTemplate.update("update person set name=?,age=?,email=?, address=? where id=?",
                updatedPerson.getName(),updatedPerson.getAge(),updatedPerson.getEmail(),id,updatedPerson.getAddress());

   }

    public void delete(int id){

        jdbcTemplate.update("DELETE from Person where id=?",id);
}


//////////////////////////////////////////////
    //////// Тестируем производительность пакетной вставки
    // /////////////////////////

    public void testMultipleUpdate(){
        List<Person> people =  create1000People();

        long before = System.currentTimeMillis();

        for (Person person : people) {
            jdbcTemplate.update("insert into person values(?,?,?)",person.getName(),person.getAge()
                    ,person.getEmail());
        }

        long after = System.currentTimeMillis();
        System.out.println("Time: "+(after-before));
    }

    public void testBatchUpdate(){
        List <Person> people = create1000People();

        long before = System.currentTimeMillis();

        jdbcTemplate.batchUpdate("insert into person values(?,?,?,?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                        preparedStatement.setInt(1,people.get(i).getId());
                        preparedStatement.setString(2,people.get(i).getName());
                        preparedStatement.setInt(3,people.get(i).getAge());
                        preparedStatement.setString(4,people.get(i).getEmail());
                    }

                    @Override
                    public int getBatchSize() {
                        return people.size();
                    }
                });

        long after = System.currentTimeMillis();
        System.out.println("Time_batch: "+(after-before));
    }

    private List<Person> create1000People(){
        List<Person> people = new ArrayList<>();

        for (int i = 0;i<1000;i++) {
            people.add(new Person(i, "Name" + i, 30, "test" + i + "mail.ru", "some address"));
        }
        return people;

        }
    }

