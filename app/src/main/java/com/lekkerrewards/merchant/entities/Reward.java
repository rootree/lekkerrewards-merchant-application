package com.lekkerrewards.merchant.entities;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.joda.time.DateTime;

@Table(name="reward", id = "id")
public class Reward extends Model
{
/*    @Column(name="id", unique = true)
    public long id;*/

    @Column(name="updated_at")
    public DateTime updatedAt;

    @Column(name="created_at")
    public DateTime createdAt;

    @Column(name="is_active")
    public boolean isActive;

    @Column(name="parent_id")
    public long parentId = 0;

    @Column(name="points")
    public int points;

    @Column(name="name")
    public String name = "";

    @Column(name="fk_merchant")
    public Merchant fkMerchant;

    @Column(name="fk_merchant_branch")
    public MerchantBranch fkMerchantBranch;

    @Column(name="fk_owner")
    public Owner fkOwner;

    @Column(name="code")
    public String code;


    public static Reward getByCode(String code) {

        return new Select()
                .from(Reward.class)
                .where("code = ?", code)
                .orderBy("id DESC")
                .executeSingle();
    }
}
