#!/bin/bash

echo ""
echo "Applying migration $packageName$.$className;format="snake"$"

echo "Adding routes to conf/$packageName$.routes"

echo "" >> ../conf/$packageName$.routes
echo "GET        /$className;format="decap"$                        $packageName$.controllers.$className$Controller.onPageLoad(mode: Mode = NormalMode)" >> ../conf/$packageName$.routes
echo "POST       /$className;format="decap"$                        $packageName$.controllers.$className$Controller.onSubmit(mode: Mode = NormalMode)" >> ../conf/$packageName$.routes

echo "GET        /change$className$                  $packageName$.controllers.$className$Controller.onPageLoad(mode: Mode = CheckMode)" >> ../conf/$packageName$.routes
echo "POST       /change$className$                  $packageName$.controllers.$className$Controller.onSubmit(mode: Mode = CheckMode)" >> ../conf/$packageName$.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.title = $className;format="decap"$" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.heading = $className;format="decap"$" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.checkYourAnswersLabel = $className;format="decap"$" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.error.required = Select yes if $className;format="decap"$" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.change.hidden = $className$" >> ../conf/messages.en

echo "Migration $packageName$.$className;format="snake"$ completed"
