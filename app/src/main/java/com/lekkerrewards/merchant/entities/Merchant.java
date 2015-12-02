package com.lekkerrewards.merchant.entities;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import org.joda.time.DateTime;


@Table(name="merchant", id = "id")
public class Merchant extends Model
{
/*    @Column(name="id", unique = true)
    public long id;*/

    @Column(name="updated_at")
    public DateTime updatedAt;

    @Column(name="created_at")
    public DateTime createdAt;

    @Column(name="is_active")
    public boolean isActive;

    @Column(name="name")
    public String name = "";

    @Column(name="facebook")
    public String facebook;

    @Column(name="twitter")
    public String twitter;


    @Column(name="yelp_id")
    public String yelpId;

    @Column(name="website")
    public String website;

    @Column(name="fk_category")
    public Category fkCategory;

}
