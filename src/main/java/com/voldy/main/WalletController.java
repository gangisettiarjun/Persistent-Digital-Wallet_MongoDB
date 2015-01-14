package com.voldy.main;

import com.mongodb.*;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.voldy.beans.BankAccount;
import com.voldy.beans.IDCard;
import com.voldy.beans.User;
import com.voldy.beans.WebLogin;
import com.voldy.exception.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//import com.mongodb.*;

@RestController
public class WalletController{

    //-----------------------------Users API----------------------------
    @RequestMapping(value="api/v1//users",method = RequestMethod.POST)

    public User createUsers(@Valid @RequestBody User user) {
        user.setId("u-"+new Date().hashCode());
        user.setCreated_at(new Date().toString());

        try {
            String textUri = "mongodb://arjun:test123@ds047030.mongolab.com:47030/walletdb";
            MongoClientURI uri = new MongoClientURI(textUri);
            MongoClient client = new MongoClient(uri);
            DB db = client.getDB("walletdb");
            DBCollection store = db.getCollection("users");
            BasicDBObject postUser = new BasicDBObject();
            postUser.put("_id", user.getId());
            postUser.put("email", user.getEmail());
            postUser.put("password", user.getPassword());
            postUser.put("created_at", user.getCreated_at());
            store.insert(postUser);
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        }
        catch (MongoException e) {
            e.printStackTrace();
        }
    	return user;
    }
    
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    ErrorMessage handleException(MethodArgumentNotValidException ex) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        List<ObjectError> globalErrors = ex.getBindingResult().getGlobalErrors();
        List<String> errors = new ArrayList<>(fieldErrors.size() + globalErrors.size());
        String error;
        for (FieldError fieldError : fieldErrors) {
            error = fieldError.getField() + ", " + fieldError.getDefaultMessage();
            errors.add(error);
        }
        for (ObjectError objectError : globalErrors) {
            error = objectError.getObjectName() + ", " + objectError.getDefaultMessage();
            errors.add(error);
        }
        return new ErrorMessage(errors);
    }

    @RequestMapping(value="api/v1//users/{user_id}",method = RequestMethod.GET)
    public BasicDBObject viewUser(@PathVariable("user_id") String uid) {
        BasicDBObject retrieveUser=null;

        try {
            String textUri = "mongodb://arjun:test123@ds047030.mongolab.com:47030/walletdb";
            MongoClientURI uri = new MongoClientURI(textUri);
            MongoClient client = new MongoClient(uri);
            DB db = client.getDB("walletdb");
            DBCollection store = db.getCollection("users");
            BasicDBObject fetchQuery = new BasicDBObject();
            fetchQuery.put("_id",uid);
            BasicDBObject fetchFields = new BasicDBObject();
            fetchFields.put("_id",1);
            fetchFields.put("email",2);
            fetchFields.put("password",3);
            fetchFields.put("created_at",4);
            fetchFields.put("updated_at",5);
            DBCursor cursor = store.find(fetchQuery, fetchFields);
            while(cursor.hasNext()) {
            retrieveUser =(BasicDBObject)cursor.next();
            }
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        }
        catch (MongoException e) {
            e.printStackTrace();
        }
        return retrieveUser;
    }


    @RequestMapping(value="api/v1//users/{user_id}",method = RequestMethod.PUT)
     public BasicDBObject updateUser(@PathVariable("user_id") String id,@RequestBody User user) {
        BasicDBObject putUser=null;
        user.setUpdated_at(new Date().toString());
        user.setId(id);
        try {
            String textUri = "mongodb://arjun:test123@ds047030.mongolab.com:47030/walletdb";
            MongoClientURI uri = new MongoClientURI(textUri);
            MongoClient client = new MongoClient(uri);
            DB db = client.getDB("walletdb");
            DBCollection store = db.getCollection("users");
            DBObject query = new BasicDBObject("_id", user.getId());
            DBObject put = new BasicDBObject().append("email",user.getEmail())
                                              .append("password", user.getPassword())
                                              .append("updated_at",user.getUpdated_at());
            DBObject update = new BasicDBObject("$set", put);
            store.updateMulti(query, update);

            BasicDBObject fetchFields = new BasicDBObject();
            fetchFields.put("_id",1);
            fetchFields.put("email",2);
            fetchFields.put("password",3);
            fetchFields.put("created_at",4);
            fetchFields.put("updated_at",5);
            DBCursor cursor = store.find(query, fetchFields);
            while(cursor.hasNext()) {
                putUser =(BasicDBObject)cursor.next();
            }
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        }
        catch (MongoException e) {
            e.printStackTrace();
        }
        return putUser;
    }
    
  //-----------------------------Users End----------------------------
    
  //---------------------Id Cards API-------------------------------------

    @RequestMapping(value="api/v1//users/{user_id}/idcards",method = RequestMethod.POST)
    public IDCard createIDCard(@RequestBody IDCard idCard, @PathVariable("user_id") String id) {

        idCard.setCard_id("c-"+new Date().hashCode());

        try {
            String textUri = "mongodb://arjun:test123@ds047030.mongolab.com:47030/walletdb";
            MongoClientURI uri = new MongoClientURI(textUri);
            MongoClient client = new MongoClient(uri);
            DB db = client.getDB("walletdb");
            DBCollection store = db.getCollection("users");
            DBObject query = new BasicDBObject("_id", id);
            BasicDBObject postId = new BasicDBObject();
            postId.put("card_id", idCard.getCard_id());
            postId.put("card_name", idCard.getCard_name());
            postId.put("card_number", idCard.getCard_number());
            postId.put("expiration_date", idCard.getExpiration_date());

            DBCursor cursor=store.find(query);
            List<BasicDBObject> idCardsList=null;
            while (cursor.hasNext()){
                DBObject ingest= cursor.next();

                if(ingest.get("IDcards")==null){
                    idCardsList=new ArrayList<BasicDBObject>();
                    idCardsList.add(postId);
                }
                else{
                    idCardsList= (ArrayList)ingest.get("IDcards");
                    idCardsList.add(postId);
                }

                ingest.put("IDcards",idCardsList);
                store.save(ingest);
            }
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        }
        catch (MongoException e) {
            e.printStackTrace();
        }
        return idCard;
    }

    @RequestMapping(value="api/v1//users/{user_id}/idcards",method = RequestMethod.GET)
    public BasicDBObject listIDCards(@PathVariable("user_id") String id) {
        BasicDBObject retrievedIDs=null;

        try {
            String textUri = "mongodb://arjun:test123@ds047030.mongolab.com:47030/walletdb";
            MongoClientURI uri = new MongoClientURI(textUri);
            MongoClient client = new MongoClient(uri);
            DB db = client.getDB("walletdb");
            DBCollection store = db.getCollection("users");

            BasicDBObject fetchQuery = new BasicDBObject();
            fetchQuery.put("_id",id);
            BasicDBObject fetchFields = new BasicDBObject();
            fetchFields.put("_id",1);
            fetchFields.put("email",2);
            fetchFields.put("password",3);
            fetchFields.put("created_at",4);
            fetchFields.put("IDcards",5);
            DBCursor cursor = store.find(fetchQuery,fetchFields);
            while(cursor.hasNext()) {
                retrievedIDs =(BasicDBObject)cursor.next();
            }
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        }
        catch (MongoException e) {
            e.printStackTrace();
        }
        return retrievedIDs;
    }

    @RequestMapping(value="api/v1//users/{user_id}/idcards/{card_id}",method = RequestMethod.DELETE)
    public void deleteIDCard(@PathVariable("user_id") String id , @PathVariable("card_id") String cid) {

        try {
            String textUri = "mongodb://arjun:test123@ds047030.mongolab.com:47030/walletdb";
            MongoClientURI uri = new MongoClientURI(textUri);
            MongoClient client = new MongoClient(uri);
            DB db = client.getDB("walletdb");
            DBCollection store = db.getCollection("users");

            BasicDBObject query = new BasicDBObject("_id", id);

            BasicDBObject pull = new BasicDBObject(
                    "$pull", new BasicDBObject(
                    "IDcards", new BasicDBObject(
                    "card_id", cid)));
            store.update(query, pull);
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        }
        catch (MongoException e) {
            e.printStackTrace();
        }

    }
  //---------------------Id Cards End-------------------------------------
    
  //---------------------Web Logins API--------------------------------
    
    @RequestMapping(value="api/v1//users/{user_id}/weblogins",method = RequestMethod.POST)
        public WebLogin createWebLogin(@RequestBody WebLogin webLogin, @PathVariable("user_id") String id) {

        List<BasicDBObject> webLoginList  = new ArrayList<BasicDBObject>();
        webLogin.setLogin_id("l-" + new Date().hashCode());

        try {
            String textUri = "mongodb://arjun:test123@ds047030.mongolab.com:47030/walletdb";
            MongoClientURI uri = new MongoClientURI(textUri);
            MongoClient client = new MongoClient(uri);
            DB db = client.getDB("walletdb");
            DBCollection store = db.getCollection("users");
            DBObject query = new BasicDBObject("_id", id);

            BasicDBObject postWlogin = new BasicDBObject();
            postWlogin.put("login_id", webLogin.getLogin_id());
            postWlogin.put("url", webLogin.getUrl());
            postWlogin.put("login", webLogin.getLogin());
            postWlogin.put("password", webLogin.getPassword());

            DBCursor cursor=store.find(query);
            List<BasicDBObject> wLoginList=null;
            while (cursor.hasNext()){
                DBObject ingest= cursor.next();

                if(ingest.get("WebLogins")==null){
                    wLoginList=new ArrayList<BasicDBObject>();
                    wLoginList.add(postWlogin);
                }
                else{
                    wLoginList= (ArrayList)ingest.get("WebLogins");
                    wLoginList.add(postWlogin);
                }

                ingest.put("WebLogins",wLoginList);
                store.save(ingest);
            }
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        }
        catch (MongoException e) {
            e.printStackTrace();
        }
        return webLogin;
    }
   
 
    @RequestMapping(value="api/v1//users/{user_id}/weblogins",method = RequestMethod.GET)
    public BasicDBObject listWebLogins(@PathVariable("user_id") String id) {

        BasicDBObject retrievedLogins=null;

        try {
            String textUri = "mongodb://arjun:test123@ds047030.mongolab.com:47030/walletdb";
            MongoClientURI uri = new MongoClientURI(textUri);
            MongoClient client = new MongoClient(uri);
            DB db = client.getDB("walletdb");
            DBCollection store = db.getCollection("users");

            BasicDBObject fetchQuery = new BasicDBObject();
            fetchQuery.put("_id",id);
            BasicDBObject fetchFields = new BasicDBObject();
            fetchFields.put("_id",1);
            fetchFields.put("email",2);
            fetchFields.put("password",3);
            fetchFields.put("created_at",4);
            fetchFields.put("WebLogins",5);
            DBCursor cursor = store.find(fetchQuery,fetchFields);
            while(cursor.hasNext()) {
                retrievedLogins =(BasicDBObject)cursor.next();
            }
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        }
        catch (MongoException e) {
            e.printStackTrace();
        }
        return retrievedLogins;
    }
   
     @RequestMapping(value="api/v1//users/{user_id}/weblogins/{login_id}",method = RequestMethod.DELETE)
    public void deleteWebLogin(@PathVariable("user_id") String id , @PathVariable("login_id") String lid) {

         try {
             String textUri = "mongodb://arjun:test123@ds047030.mongolab.com:47030/walletdb";
             MongoClientURI uri = new MongoClientURI(textUri);
             MongoClient client = new MongoClient(uri);
             DB db = client.getDB("walletdb");
             DBCollection store = db.getCollection("users");

             BasicDBObject query = new BasicDBObject("_id", id);

             BasicDBObject pull = new BasicDBObject(
                     "$pull", new BasicDBObject(
                     "WebLogins", new BasicDBObject(
                     "login_id", lid)));
             store.update(query, pull);
         }
         catch (UnknownHostException e) {
             e.printStackTrace();
         }
         catch (MongoException e) {
             e.printStackTrace();
         }
    }
    
     //---------------------Web Logins End--------------------------------  
  
    //---------------------Bank Accounts API------------------------------
    
    @RequestMapping(value="api/v1//users/{user_id}/bankaccounts",method = RequestMethod.POST)
    public BankAccount createBankAccount(@RequestBody BankAccount bankAccount, @PathVariable("user_id") String id)
    throws UnirestException {

          HttpResponse<JsonNode> response=Unirest.get("http://www.routingnumbers.info/api/data.json")
                                                 .field("rn",bankAccount.getRouting_number()).asJson();
          JsonNode body =response.getBody();
          if(String.valueOf(body.getObject().get("code")).equals("200"))
          {
              bankAccount.setAccount_name(String.valueOf(body.getObject().get("customer_name")));
          }
          else
              throw new UnirestException("");

          bankAccount.setBa_id("b-" + new Date().hashCode());

          try {
              String textUri = "mongodb://arjun:test123@ds047030.mongolab.com:47030/walletdb";
              MongoClientURI uri = new MongoClientURI(textUri);
              MongoClient client = new MongoClient(uri);
              DB db = client.getDB("walletdb");
              DBCollection store = db.getCollection("users");
              DBObject query = new BasicDBObject("_id", id);
              DBCursor cursor = store.find(query);

              BasicDBObject postAccount = new BasicDBObject();
              postAccount.put("ba_id", bankAccount.getBa_id());
              postAccount.put("account_name", bankAccount.getAccount_name());
              postAccount.put("routing_number", bankAccount.getRouting_number());
              postAccount.put("account_number", bankAccount.getAccount_number());
              List<BasicDBObject> bankAccountList=null;

              while(cursor.hasNext()){

                  DBObject ingest= cursor.next();
                      if (ingest.get("BankAccounts")==null) {
                      bankAccountList = new ArrayList<BasicDBObject>();
                      bankAccountList.add(postAccount);
                  }
                  else{
                      bankAccountList= (ArrayList) ingest.get("BankAccounts");
                      bankAccountList.add(postAccount);
                  }

                  ingest.put("BankAccounts",bankAccountList);
                  store.save(ingest);
              }
          }
          catch (UnknownHostException e) {
              e.printStackTrace();
          }
          catch (MongoException e) {
              e.printStackTrace();
          }
          return bankAccount;

    }


    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    void handleException(UnirestException e) {e.printStackTrace();}
    
    @RequestMapping(value="api/v1//users/{user_id}/bankaccounts",method = RequestMethod.GET)
    public BasicDBObject listBankAccount(@PathVariable("user_id") String id) {
        BasicDBObject retrievedAccounts=null;

        try {
            String textUri = "mongodb://arjun:test123@ds047030.mongolab.com:47030/walletdb";
            MongoClientURI uri = new MongoClientURI(textUri);
            MongoClient client = new MongoClient(uri);
            DB db = client.getDB("walletdb");
            DBCollection store = db.getCollection("users");

            BasicDBObject fetchQuery = new BasicDBObject();
            fetchQuery.put("_id",id);
            BasicDBObject fetchFields = new BasicDBObject();
            fetchFields.put("_id",1);
            fetchFields.put("email",2);
            fetchFields.put("password",3);
            fetchFields.put("created_at",4);
            fetchFields.put("BankAccounts",5);
            DBCursor cursor = store.find(fetchQuery,fetchFields);
            while(cursor.hasNext()) {
                retrievedAccounts =(BasicDBObject)cursor.next();
            }
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        }
        catch (MongoException e) {
            e.printStackTrace();
        }
        return retrievedAccounts;
    }
    
     @RequestMapping(value="api/v1//users/{user_id}/bankaccounts/{ba_id}",method = RequestMethod.DELETE)
        public void deleteBankAccount(@PathVariable("user_id") String id , @PathVariable("ba_id") String bid) {

         try {
             String textUri = "mongodb://arjun:test123@ds047030.mongolab.com:47030/walletdb";
             MongoClientURI uri = new MongoClientURI(textUri);
             MongoClient client = new MongoClient(uri);
             DB db = client.getDB("walletdb");
             DBCollection store = db.getCollection("users");

             BasicDBObject query = new BasicDBObject("_id", id);

             BasicDBObject pull = new BasicDBObject(
                     "$pull", new BasicDBObject(
                     "BankAccounts", new BasicDBObject(
                     "ba_id", bid)));
             store.update(query, pull);
         }
         catch (UnknownHostException e) {
             e.printStackTrace();
         }
         catch (MongoException e) {
             e.printStackTrace();
         }
    }
  //---------------------Bank Accounts End------------------------------
  }
