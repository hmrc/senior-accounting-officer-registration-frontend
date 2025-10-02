#!/bin/bash


MESSAGES_FILE=../conf/messages.en
ROUTES_FILE=../conf/app.routes

echo ""
echo "Applying migration $className;format="snake"$"

echo "Adding routes to conf/app.routes"
{
  echo ""
  echo "GET        /$className;format="decap"$                       controllers.$className$Controller.onPageLoad()"
}  >> "\$ROUTES_FILE"

echo "Adding messages to conf.messages"
{
  echo ""
  echo "$className;format="decap"$.title = $className;format="decap"$"
  echo "$className;format="decap"$.heading = $className;format="decap"$"
  echo "$className;format="decap"$.task.status.cannotStartYet = Cannot start yet"
  echo "$className;format="decap"$.task.status.completed = Completed"
  echo "$className;format="decap"$.task.status.notStarted = NotStarted"
} >> "\$MESSAGES_FILE"


i=1
while [ \$i -le $numberOfTasks$ ]
do
  echo "$className;format="decap"$.task.\$i.text = Task \$i" >> "\$MESSAGES_FILE"
  i=\$((i+1))
done

echo ""
echo "Migration $className;format="snake"$ completed"
