package com.lekkerrewards.merchant.entities;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;



@Table(name="country", id = "id")
public class Country extends Model
{

/*    @Column(name="id", unique = true)
    public long id;*/

   @Column(name="name")
   public String name = "";

    @Column(name="iso")
    public String iso = "";


    @Column(name="nicename")
    public String nicename = "";


    @Column(name="iso3")
    public String iso3 = "";

   @Column(name="numcode")
   public int numcode;

   @Column(name="phonecode")
   public int phonecode;



}
