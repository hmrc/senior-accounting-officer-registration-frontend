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
echo "$packageName$.$className;format="decap"$.title = $title$" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.heading = $title$" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.$option1key;format="decap"$ = $option1msg$" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.$option2key;format="decap"$ = $option2msg$" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.checkYourAnswersLabel = $title$" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.error.required = Select $className;format="decap"$" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.change.hidden = $className$" >> ../conf/messages.en

echo "Adding to $packageName$.ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitrary$className$: Arbitrary[$className$] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf($className$.values)";\
    print "    }";\
    next }1' ../test-utils/generators/$packageName$/ModelGenerators.scala > tmp && mv tmp ../test-utils/generators/$packageName$/ModelGenerators.scala

echo "Migration $className;format="snake"$ completed"
