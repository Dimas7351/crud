package org.example.dao;

import org.example.models.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    public Person show(int id){
       return jdbcTemplate.query("SELECT * FROM Person where id=?",new Object[]{id},new PersonMapper())
               .stream().findAny().orElse(null);
    }

    public void save(Person person){
        jdbcTemplate.update("insert into person values(1,?,?,?)",person.getName(),person.getAge()
        ,person.getEmail());
    }

    public void update(int id, Person updatedPerson){

        jdbcTemplate.update("update person set name=?,age=?,email=? where id=?",
                updatedPerson.getName(),updatedPerson.getAge(),updatedPerson.getEmail(),id);

   }

    public void delete(int id){

        jdbcTemplate.update("DELETE from Person where id=?",id);
}}
