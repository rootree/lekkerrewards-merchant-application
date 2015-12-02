package com.lekkerrewards.merchant.entities;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.joda.time.DateTime;

import java.util.List;

@Table(name="merchant_branch", id = "id")
public class MerchantBranch extends Model
{
/*    @Column(name="id", unique = true)
    public long id;*/

    @Column(name="updated_at")
    public DateTime updatedAt;

    @Column(name="created_at")
    public DateTime createdAt;

    @Column(name="is_active")
    public boolean isActive;

    @Column(name="longitude")
    public long longitude;

    @Column(name="latitude")
    public long latitude;

    @Column(name="phone_number")
    public String phoneNumber = "";

    @Column(name="address")
    public String address = "";

    @Column(name="zipcode")
    public String zipcode = "";

   @Column(name="permalink_path")
   public String permalinkPath = "";

    @Column(name="time_offset")
    public String timeOffset = "+02:00";

    @Column(name="api_key")
    public String apiKey = "";

    @Column(name="e_mail")
    public String eMail;

    @Column(name="fk_city")
    public City fkCity;

    @Column(name="fk_country")
    public Country fkCountry;

    @Column(name="fk_merchant")
    public Merchant fkMerchant;

    @Column(name="fk_state")
    public State fkState;

    public static List<Reward> getActiveRewards(MerchantBranch merchantBranch) {
        return new Select()
                .from(Reward.class)
                .where("fk_merchant_branch = ?", merchantBranch.getId())
                .where("is_active = 1")
                .orderBy("Points ASC")
                .execute();
    }

    public static MerchantBranch getByAPIKey(String APIKey) {

        return new Select()
                .from(MerchantBranch.class)
                .where("api_key = ?", APIKey)
                .orderBy("id DESC")
                .executeSingle();
    }
}
