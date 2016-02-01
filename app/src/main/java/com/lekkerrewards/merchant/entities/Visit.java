package com.lekkerrewards.merchant.entities;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.lekkerrewards.merchant.Config;
import com.lekkerrewards.merchant.LekkerApplication;

import org.joda.time.DateTime;

import java.util.List;


@Table(name="visit", id = "id")
public class Visit extends Model
{
/*
    @Column(name="id")
    public long id;
*/

    @Column(name="updated_at")
    public DateTime updatedAt;

    @Column(name="created_at")
    public DateTime createdAt;

    @Column(name="status")
    public int status;

    @Column(name="obtained_points")
    public int obtainedPoints = 0;

    @Column(name="fk_customer")
    public Customer fkCustomer;

    @Column(name="fk_merchant")
    public Merchant fkMerchant;

    @Column(name="fk_merchant_branch")
    public MerchantBranch fkMerchantBranch;


    public static List<Visit> getVisits(
            MerchantBranch merchantBranch,
            Customer customer
    ) {
        return new Select()
                .from(Visit.class)
                .where("fk_merchant_branch = ?", merchantBranch.getId())
                .where("fk_customer = ?", customer.getId())
                .where("status = 1")
                .orderBy("id DESC")
                .limit(Config.LIMIT_VISITS)
                .execute();
    }


    public static int getCountOfAllVisits(
            MerchantBranch merchantBranch
    ) {
        return ((List) new Select()
                .from(Visit.class)
                .where("fk_merchant_branch = ?", merchantBranch.getId())
                .where("status = 1")
                .execute()).size();
    }
}
