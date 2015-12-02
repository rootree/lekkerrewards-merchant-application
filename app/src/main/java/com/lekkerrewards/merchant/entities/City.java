package com.lekkerrewards.merchant.entities;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name="city", id = "id")
public class City extends Model
{
/*    @Column(name="id", unique = true)
    public long id;*/

    @Column(name="name")
    public String name = "";

    @Column(name="fk_country")
    public long fkCountry;

    @Column(name="fk_state")
    public State fkState;
}
