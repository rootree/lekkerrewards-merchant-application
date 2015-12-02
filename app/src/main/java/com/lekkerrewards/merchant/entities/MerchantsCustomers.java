package com.lekkerrewards.merchant.entities;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.joda.time.DateTime;



@Table(name="merchants__customers", id = "id")
public class MerchantsCustomers extends Model
{

/*    @Column(name="id", unique = true)
    public long id;*/

    @Column(name="updated_at")
    public DateTime updatedAt;

    @Column(name="created_at")
    public DateTime createdAt;

   @Column(name="first_at")
   public DateTime firstAt;

    @Column(name="visits")
    public int visits = 0;

   @Column(name="redeems")
   public int redeems = 0;

    @Column(name="points")
    public int points = 0;

    @Column(name="fk_customer")
    public Customer fkCustomer;

    @Column(name="fk_merchant")
    public Merchant fkMerchant;

    @Column(name="fk_merchant_branch")
    public MerchantBranch fkMerchantBranch;

    public static MerchantsCustomers getMerchantCustomerRelation(
            MerchantBranch merchantBranch,
            Customer customer
    ) {
        return new Select()
                .from(MerchantsCustomers.class)
                .where("fk_merchant_branch = ?", merchantBranch.getId())
                .where("fk_customer = ?", customer.getId())
                .orderBy("id ASC")
                .executeSingle();
    }
}
