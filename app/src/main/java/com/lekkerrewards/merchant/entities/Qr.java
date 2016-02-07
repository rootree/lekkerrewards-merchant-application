package com.lekkerrewards.merchant.entities;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.lekkerrewards.merchant.LekkerApplication;

import org.joda.time.DateTime;


@Table(name="qr", id = "id")
public class Qr extends Model
{

/*    @Column(name="id", unique = true)
    public long id;*/

    @Column(name="updated_at")
    public DateTime updatedAt;

    @Column(name="code")
    public int code;

    @Column(name="status")
    public int status;

    @Column(name="source")
    public int source;

    @Column(name="created_at")
    public DateTime createdAt;

    @Column(name="last_used")
    public DateTime lastUsed;

    @Column(name="fk_customer")
    public Customer fkCustomer;


    public static Qr getQRByCode(int code) {

        return new Select()
                .from(Qr.class)
                .where("code = ?", code)
                .orderBy("id DESC")
                .executeSingle();
    }

    public static Qr getQRByCustomer(Customer customer) {

        return new Select()
                .from(Qr.class)
                .where("fk_customer = ?", customer.getId())
                .where("status = ?", LekkerApplication.QR_STATUS_ACTIVATED)
                .orderBy("id DESC")
                .executeSingle();
    }

    public static void deactivateByCustomer(Customer customer) {

        new Update(Qr.class)
                .set("status = ?", LekkerApplication.QR_STATUS_DEACTIVATED)
                .where("fk_customer = ?", customer.getId())
                .execute();
    }
}
