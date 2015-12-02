package com.lekkerrewards.merchant.entities;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.joda.time.DateTime;


@Table(name="owner", id = "id")
public class Owner extends Model
{
/*    @Column(name="id", unique = true)
    public long id;*/

    @Column(name="updated_at")
    public DateTime updatedAt;

    @Column(name="created_at")
    public DateTime createdAt;

    @Column(name="name")
    public String name = "";

    @Column(name="e_mail")
    public String eMail = "";

    @Column(name="password")
    public String password = "";

    @Column(name="birthday")
    public DateTime birthday;

    @Column(name="gender")
    public String gender;

    @Column(name="phone_number")
    public String phoneNumber;

    @Column(name="fk_merchant_branch")
    public MerchantBranch fkMerchantBranch;

    @Column(name="fk_merchant")
    public Merchant fkMerchant;


    public static Owner getByBranch(MerchantBranch merchantBranch) {

        return new Select()
                .from(Owner.class)
                .where("fk_merchant_branch = ?", merchantBranch.getId())
                .orderBy("id DESC")
                .executeSingle();
    }

}
