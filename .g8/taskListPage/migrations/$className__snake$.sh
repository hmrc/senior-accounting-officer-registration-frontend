#!/bin/bash

echo ""
echo "Applying migration $className;format="snake"$"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /$className;format="decap"$                       controllers.$className$Controller.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "$className;format="decap"$.title = $className;format="decap"$" >> ../conf/messages.en
echo "$className;format="decap"$.heading = $className;format="decap"$" >> ../conf/messages.en


i=1
while [ $$i -le $numberOfTasks$ ]
do
  echo "$className;format="decap"$.task.$$i.text = Task $$i" >> ../conf/messages.en
  i=$$((i+1))
done

echo ""
echo "Migration $className;format="snake"$ completed"
