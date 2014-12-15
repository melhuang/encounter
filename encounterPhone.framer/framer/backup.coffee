# This imports all the layers for "Mockups" into mockupsLayers
mockupsLayers = Framer.Importer.load "imported/Mockups"

Framer.Defaults.Animation =
	curve: "spring(500,30,0)", time: 0.5
	#curve: "ease-in-out", time: 0.5

starImage = mockupsLayers['Star1'].image
makeStar = (star) ->
	starred = false
	star.on Events.Click, ->
		Done.superLayer = Welcome
		Done.y = Skip.y
		Skip.visible = false		
		if !starred
			star.image = mockupsLayers['GreenStar'].image
			starred = true
		else
			star.image = starImage
			starred = false

for layerName of mockupsLayers
	console.log(layerName)
	window[layerName] = mockupsLayers[layerName]
	if layerName.substring(0,4) == "Star"
		makeStar(mockupsLayers[layerName])
		
backgroundLayer = new BackgroundLayer backgroundColor:"white"
	
SkipBtn = new Layer({superLayer: Welcome, x:900, y:40, width:150, opacity:0})
SkipBtn.on Events.Click, ->
	Welcome.bringToFront()
	Welcome.states.next()
	Main.visible = true

Initial.states.add
	moveLeft: {x: -1080}
Welcome.states.add
	moveDown: {y: 1920}
Welcome.backgroundColor = "white"

Initial.on Events.Click, ->
	Initial.states.next()
	Initial.bringToFront()
	Welcome.visible = true
	
searchAndFriends.x = 18
searchAndFriends.y = 476
searchAndFriends.superLayer = Welcome
searchAndFriends.draggable.enabled = true
searchAndFriends.draggable.speedX = 0

initY = searchAndFriends.y
initSYFy = starYourFriends.y
lastY = 258
searchAndFriends.on Events.DragMove, ->
	if searchAndFriends.y > initY
		searchAndFriends.y = initY
		return
	deltaY = initY - searchAndFriends.y
	if searchAndFriends.y <= lastY
		searchAndFriends.y = lastY
		green.height = searchAndFriends.y - 25
		starYourFriends.y = 90
		welcome.opacity = 0
		searchAndFriends.draggable.enabled = false
		#friendBubbles2.draggable.enabled = true
		#friendBubbles2.draggable.speedX = 0
		return
	if starYourFriends.y > 90
		starYourFriends.y = initSYFy - deltaY
	green.height = searchAndFriends.y - 25
	welcome.opacity = 1-deltaY/(initY-lastY)
	Skip.y = 56+(69-56)*(1-deltaY/(initY-lastY))
	Done.y = Skip.y
	
StatusDND.visible = false
StatusActive.on Events.Click, ->
	StatusActive.visible = false
	StatusDND.visible = true
StatusDND.on Events.Click, ->
	StatusDND.visible = false
	StatusActive.visible = true
Map.x = 1080
Map.states.add
	slideIn: {x: 0}
MapBtn.on Events.Click, ->
	Map.states.next()
Map.visible = true
Map.on Events.Click, ->
	Map.states.next()
Friends.y = 1920
Friends.states.add
	slideIn: {y: 0}
FriendsBtn.on Events.Click, ->
	Friends.visible = true
	FriendsStarred.visible = false
	Friends.states.next()
FriendsRecent.superLayer = Friends
FriendsStarred.superLayer = Friends
DoneBtn = new Layer({superLayer: Friends, x:900, y:40, width:150, opacity:0})
StarredBtn = new Layer({superLayer: Friends, x:50, y:270, width:330, opacity:0})
AllBtn = new Layer({superLayer: Friends, x:330+50, y:270, width:310, opacity:0})
RecentBtn = new Layer({superLayer: Friends, x:320*2+50, y:270, width:310, opacity:0})
DoneBtn.on Events.Click, ->
	Friends.states.next()
StarredBtn.on Events.Click, ->
	FriendsStarred.visible = true
	FriendsRecent.visible = false
	FriendsStarred.y = 0
AllBtn.on Events.Click, ->
	FriendsStarred.visible = false
	FriendsRecent.visible = false
	Friends.visible = true
RecentBtn.on Events.Click, ->
	FriendsRecent.visible = true
	FriendsStarred.visible = false