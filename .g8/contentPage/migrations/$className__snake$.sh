#!/bin/bash

echo ""
echo "Applying migration $packageName$.$className;format="snake"$"

echo "Adding routes to conf/$packageName$.routes"
echo "" >> ../conf/$packageName$.routes
echo "GET        /$className;format="decap"$                       $packageName$.controllers.$className$Controller.onPageLoad()" >> ../conf/$packageName$.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.title = $className;format="decap"$" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.heading = $className;format="decap"$" >> ../conf/messages.en

echo "Migration $packageName$.$className;format="snake"$ completed"
