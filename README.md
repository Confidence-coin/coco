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

 - :notebook::green_book: Create wallet-password and use it to encrypt all the settings files including the wallet private key.

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
- :notebook: Fetch balance data from the pool and update the balance **100 points**

# Send coins  
![send coins](https://user-images.githubusercontent.com/6512430/42005573-3b8744ba-7a43-11e8-8ea1-94dc8c9c31b4.PNG)   

Make transactions to one or multiple recipients.
 - :orange_book: Create a listView to display the transactions **100 points**
 - :orange_book: Allow transactions addition, deletion and modification **100 points**
 - :notebook::green_book: Create transaction model where the final data of the transaction will be saved
 - :notebook::green_book: Implement one to one transaction in both the Wallet and the Pool.
 - :notebook: Implement one to many transaction with same amount in both the Wallet and the Pool. **200**
 - :notebook: Implement one to many transaction with different amount in both the Wallet and the Pool. **200**
 - :notebook: Implement the send all feature in both the Wallet and the Pool. **200**

# transaction summary 
![Transaction summary](https://user-images.githubusercontent.com/6512430/42005575-3b9e26f8-7a43-11e8-8534-68aae09f3fd5.PNG)   

The tasks from the previous screen also apply this one

# Mining screen  
![mining screen](https://user-images.githubusercontent.com/6512430/42005572-3b786558-7a43-11e8-98f9-ae937a83c927.PNG)  

  - :orange_book::green_book: Show mining speed while mining
  - :notebook::green_book: Mine and submit work to the pool
  - :notebook: Use the same functionality as in the settings screen to switch pools **50 points**

# Smart contracts  
![smart contracts](https://user-images.githubusercontent.com/6512430/42005574-3b920fd0-7a43-11e8-8cae-8a5ac0fcf974.PNG)   

The smart contract is made of two things, media and script. The media can be a text file, an image or a video, basicaly it's an HTML page that you can render offline after downloading. The HTML page and the script should be displayed and rendered separately, so one will not be able to hide the actual script.

 - :notebook: Create a page that can load HTML from a zip file. That zip file should be downloaded from a link provided by the user. **300 points**
 
 I am still not sure how the actual script will be implemented.

# Other clients
 - :orange_book: Android client. I prefer that it to be part of the IntelliJ project, but if you want to do it a standalone version, I am fine with it too. The first task is to implement the UI for all the screens. **500 points**
 - :orange_book: iPhone client. There will be no mining in iPhone client as it against Apple policy, like in Android client, let's start with UI first. **500 points**
 - :orange_book: Web client. Let's use Angular here. It should not have a mining option. **500 points**
 
 # Even more tasks
 - :notebook: Implement Pool to Pool protocol to exchange blocks. It must use UDP. The idea is that each Pool can download what block he likes from any other Pool. Anyone can use this protocol, but there will a priority for Royal network Pools. **500 points**
