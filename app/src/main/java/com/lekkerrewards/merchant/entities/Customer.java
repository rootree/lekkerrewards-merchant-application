package com.lekkerrewards.merchant.entities;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.joda.time.DateTime;


@Table(name="customer", id = "id")
public class Customer extends Model
{
/*    @Column(name="id", unique = true)
    public long id;*/

    @Column(name="updated_at")
    public DateTime updatedAt;


     @Column(name="created_at")
     public DateTime createdAt;


    @Column(name="is_photo_uploaded")
    public boolean isPhotoUploaded = false;

    @Column(name="birthday")
    public DateTime birthday;

    @Column(name="name")
    public String name = "";

   @Column(name="e_mail")
   public String eMail = "";

    @Column(name="gender")
    public String gender;

   @Column(name="password")
   public String password = "";

    @Column(name="phone_number")
    public String phoneNumber;

    public static Customer getCustomerByEmail(String eMail) {

        return new Select()
                .from(Customer.class)
                .where("e_mail = ?", eMail)
                .orderBy("id DESC")
                .executeSingle();
    }
}
