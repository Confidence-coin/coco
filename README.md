# Confidence Coin
To be released by May 2019
-------------------
You can read about Coco and find the **White Papers** on the official website: [confidence-coin.com](https://confidence-coin.com)  
Subscribe for news and release announcement [here](https://groups.google.com/forum/#!forum/confidence-coin/join)  
For any question you have the best way to contact me is via the Facebook group [Coco Developers and designers](https://www.facebook.com/groups/349187585612732/).
_________

Join the contributors' program and start earning points, once subscribing to the [mailing list](https://groups.google.com/forum/#!forum/confidence-coin/join) you can access to the [leaderboar](https://docs.google.com/spreadsheets/d/13Pk-x2AXiU3egC8lrQJrlpoAYsEdWHhLNRxgudZ4YF8) where you can see how other doing.

Start by watching the [Getting started video](https://www.youtube.com/watch?v=4eC-v7ZS3PI)

# Development and design tasks for contributors
 - :notebook: **Open dev task**
 - :orange_book: **Open design task**
 - :green_book: **Completed task**

Below are screenshots from the wallet app, I am using those to aggregate the relevant tasks.

# Create password
![create password](https://user-images.githubusercontent.com/6512430/42005570-3b5e4556-7a43-11e8-8958-770251c16629.PNG)  

 - :green_book: Create wallet-password and use it to encrypt all the settings files including the wallet private key.

# Create or restore wallet
![create or restore wallet](https://user-images.githubusercontent.com/6512430/42005569-3b49e818-7a43-11e8-9611-43e6948097b7.PNG)  

 Implement the restore wallet screen  
 - :orange_book: Screen layout layout **20 points**
 - :notebook: Restore the wallet using the private key as an input **100 points**
 - :notebook: Restore and export the wallet using [Mnemonic_phrase](https://en.bitcoin.it/wiki/Mnemonic_phrase) **400** points

Implement multi-wallets(keys) support
 - :notebook: Add wallet nickname and show it on the main screen. **50 points**
 - :notebook: Allow a user to create multiple wallets, each new wallet private key is a hash of the previous private key, so the user only needs one private key to restore all of his wallets. **200 points**
 - :orange_book: Allow the user to switch between the wallets on the Main screen **100 points**

# Main screen  
![main screen](https://user-images.githubusercontent.com/6512430/42005571-3b6ba57a-7a43-11e8-93e9-10a09cf87e2b.PNG)  

Leaderboard screen - At the moment we use Google Sheets to display the contributors' leaderboard. I would like to make our web version of it to pull the data from Google Sheets.
 - :orange_book: Create webpage ui **50 points**
 - :notebook: Add google login with access to drive permission - **70 points**
 - :notebook: Pull data from google sheet and display it on the leaderboard - **100 points**
 
 
 Link to the crypto card button. Maybe we need a better name for this button. I would like this button to take you to a screen where you can join to [D.T.P](https://confidence-coin.com/home/dtp/), as well as terminate your membership there. From the moment you joined the D.T.P, all the wallet transactions should go through the D.T.P
 - :orange_book: Create the screen - **20 points**
 - :notebook: Create a protocol of communication between the D.T.P and the wallet **150 points**
 
 Create a setting screen and a button to go there from the main screen
 - :orange_book: Create the settings screen and the button to go there **50 points**
 - :notebook: Fetch the Royal Pools domains from the current Pool. You need to add a new API call for that in the Pool **250 points**
 - :notebook: Create a settings option to select to what pool you like to connect, it can be one of the Royal pools or manually added new Pool by the user **100 points**.

User balance
- :notebook: Fetch balance data from the pool and update the balance- **100 points**

# Send coins  
![send coins](https://user-images.githubusercontent.com/6512430/42005573-3b8744ba-7a43-11e8-8ea1-94dc8c9c31b4.PNG)   
Ui/Ux and layout  
You can make multiple transactions in one call. Those transactions need to be shown on the screen in some table(List view) where you can edit or remove them. Create the layout for that view - 50 points

*Read about all I got in the white papers, it's a list feature that will send all of your available balance.*

Development tasks   
 - The client can't calculate the transactions fees as it doesn't know which wallet were registered already and which is not. For the wallets who wasn't registered one need to add them to wallets list, and it cost more transaction fees, 32 bytes of the public key. Therefore there the confirmation page where a request for calculating the transaction fees is made. I didn't implement this part yet, but I do have a 1 to 1 transaction implemented on the next screen, so you can figure out how this part could be implemented. **800 points**

# transaction summary 
![Transaction summary](https://user-images.githubusercontent.com/6512430/42005575-3b9e26f8-7a43-11e8-8534-68aae09f3fd5.PNG)   
Ui/Ux and layout  
 - Also in here, we need the list of transactions I would say that you cannot edit anything in this screen, but you can return and edit it all. **50 points**
 
Development tasks   
 - We need to save the transactions in some place. So we need to create a model for that, all my models implement Singelton interface, and you get a reference to them by calling `Factory.inject(MyModel.class)` 100 points
 - We need to implement the rest of the transactions. It includes updating the [protocol doc](https://confidence-coin.com/client-pool-protocol/). I don't know yet of a good way to maintain it, so just post it on the facebook page, and I will update the site.
         - Update the protocol - 50 points  
         - Update server side - 50 points  
         - Update client side - 50 points   
         
The above tasks are for each of the transactions except the update transaction. This one is special. I think it should not be part of the client. The D.T.P will have to implement it by them self, so we only need to update the protocol and the server side here.   
 - Server-side - 100 points
 - update protocol - 50 points
 - List group - I explain it in the white papers, it allows the D.T.P make other types of transactions besides the Update Transactions. 100 points

# Mining screen  
![mining screen](https://user-images.githubusercontent.com/6512430/42005572-3b786558-7a43-11e8-98f9-ae937a83c927.PNG)  
Ui/Ux and layout  
Think about what you want to see on this screen regarding UI/UX and post suggestions in the facebook group.

Development tasks  
 - The error handling needs improvement. I will leave it up to you how to implement it. 100 points
 - The mining algorithm needs to change, I want to add another hashing to the last step, this in order to be able to send the last two hashes as part of the block header, and whoever receive the block will verify the proof of work first without the need to download the entire block in order to calculate the transactions hash and verify it. This is the most robust verification, and it can be done in the first 64 bytes of the block. 200 points
 
# Smart contracts  
![smart contracts](https://user-images.githubusercontent.com/6512430/42005574-3b920fd0-7a43-11e8-8cae-8a5ac0fcf974.PNG)   
Ui/Ux and layout  
Let's talk in the facebook group about improving the design of this page

Development tasks  
There should be some basic protocol where we get a link, and we download a content + a script. Then we show the content, compile the script and calculate the hash of the merge. This hash is the key to the smart contract.
 
 - Create the protocol - 50 points
 - Download and show the media file from URL
     - HTML content - 50 points
     - Video content - 50 points
     - txt cointent - 25 points(the html should parse this)
     - PDF content - 80 points 
     - sound content - 50 points
     - Other content - ask in facebook group
 - The biggest part here is the script. Lets talk in the facebook group about what is should have.


# Other tasks 
For those tasks, let's discuss in the facebook group
 - Pool to pool protocol, broadcast blocks and transactions.
 - More contributors features, I would like to create a fun and a social place for everyone to play in :)
