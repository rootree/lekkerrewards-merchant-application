package com.lekkerrewards.merchant.entities;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;


@Table(name="state", id = "id")
public class State extends Model
{
/*    @Column(name="id", unique = true)
    public long id;*/

    @Column(name="name")
    public String name = "";

    @Column(name="fk_country")
    public Country fkCountry;
}
