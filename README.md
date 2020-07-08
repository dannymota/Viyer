Original App Design Project - README
===

# Name - Viyer

## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)

## Overview
### Description
Marketplace app to sell used/new items with AR functionality to increase selling rate since buyers can visualize the item before meeting up. Security features is a major focus with features such as both parties press "I'm here" when meeting up and required ID verification.

### App Evaluation
[Evaluation of your app across the following attributes]
- **Category:** Marketplace
- **Mobile:** All of the functionality lies within the app, and is compatible with a smart phone, tablet or other compatible device.
- **Story:** Removes the uncertainty when it comes to buying things online. Craigslist did not take security into consideration which led to less sales, prioritizing the user experience.
- **Market:** Anyone looking to get rid of old items and those looking for a good deal
- **Habit:** Since everyone is looking for a discount on new/old items, I'd imagine users looking through the app often to find deals forming a habit.
- **Scope:** V1 would allow sellers to post items and buyers to view the description of said items. V2 would incorpate Google Cloud Vision API to automatically verify someones identity with their ID. V3 adds AR technology allowing sellers to scan their items and buyers being able to view them.

## Product Spec

### 1. User Stories (Required and Optional)

**Required Must-have Stories**

* Your app has multiple views
* Your app interacts with a database (e.g. Parse)
* You can log in/log out of your app as a user
* You can sign up with a new user profile
* Somewhere in your app you can use the camera to take a picture and do something with the picture (e.g. take a photo and share it to a feed, or take a photo and set a user’s profile picture)
* Your app integrates with a SDK (e.g. Google Maps SDK, Facebook SDK)
* Your app contains at least one more complex algorithm (talk over this with your manager)
* Your app uses gesture recognizers (e.g. double tap to like, e.g. pinch to scale)
* Your app use an animation (doesn’t have to be fancy) (e.g. fade in/out, e.g. animating a view growing and shrinking)
* Your app incorporates an external library to add visual polish

**Optional Nice-to-have Stories**

* Instant identity verification given a user photo and government issued ID
* AR Object Scanning
* Paid front page ads
* Phone number login
* Live chat
* Material-UI Integration

### 2. Screen Archetypes

* Login screen
   * User can login
* Registration screen
   * User can enter their number to recieve a verification code
* Browse screen
   * User can vertically scroll throw products
* Post screen
   * User can post pictures with a description, price, and optionally scan their item for to be displayed using AR
* Settings screen
   * User can verify their identity
   * User can change their name
* Chat screen
   * User can chat with other sellers/buyers
* Product screen
   * User can view product listings
* Verification screen
   * User can upload picture and ID

### 3. Navigation

**Tab Navigation** (Tab to Screen)

* Browse screen
* Chat screen
* List item screen
* Security screen

**Flow Navigation** (Screen to Screen)

* Login screen
   => Browse screen
* Registration screen
   => Browse screen
* Post screen
   => Product screen
   => Browse screen
* Product screen
   => Chat screen
   => Browse screen
* Security screen
   => Verification screen
* Settings screen
   => Browse screen

## Wireframes
[Add picture of your hand sketched wireframes in this section]
<img src="https://i.imgur.com/QuNJ57V.png" width=600>
<img src="https://i.imgur.com/omcbGW3.png", width=600>
