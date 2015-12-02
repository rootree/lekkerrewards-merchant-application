package com.lekkerrewards.merchant.entities;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.joda.time.DateTime;


@Table(name="reward_history", id = "id")
public class RewardHistory extends Model
{
/*    @Column(name="id", unique = true)
    public long id;*/

    @Column(name="created_at")
    public DateTime createdAt;

    @Column(name="points")
    public int points;

    @Column(name="name")
    public String name = "";

    @Column(name="fk_reward")
    public Reward fkReward;

    @Column(name="code")
    public String code;




    public static RewardHistory findByCode(String code) {

        return new Select()
                .from(RewardHistory.class)
                .where("code = ?", code)
                .orderBy("id DESC")
                .executeSingle();
    }
}
