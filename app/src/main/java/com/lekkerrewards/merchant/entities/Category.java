package com.lekkerrewards.merchant.entities;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;


@Table(name="category", id = "id")
public class Category extends Model
{

/*    @Column(name="id", unique = true)
    public long id;*/

    @Column(name="name")
    public String name = "";

}
