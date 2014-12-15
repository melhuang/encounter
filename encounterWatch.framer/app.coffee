# --------------------- README --------------------
# - During demo, press any key to get an invite from the other person
# - TOGGLE otherView below to switch between Shawn's watch and Melissa's watch
# -------------------------------------------------
otherView = false

notificationState = false
invited = false
inviting = false

String::startsWith ?= (s) -> @[...s.length] is s
String::endsWith   ?= (s) -> s is '' or @[-s.length..] is s

# This imports all the layers for "WatchMockups" into watchmockupsLayers
watchmockupsLayers = Framer.Importer.load "imported/WatchMockups"

for layerName of watchmockupsLayers
	console.log(layerName)
	window[layerName] = watchmockupsLayers[layerName]
	if layerName.endsWith('View')
		watchmockupsLayers[layerName].center()
		watchmockupsLayers[layerName].scale = 0.85

text = new Layer
	width: 150
	height: 40
	superLayer: NearbyView
	backgroundColor: 'transparent'
	x: 160
	y: 98.1
text.style.font = '24px corbel'

DNDBtn.visible = false
ActiveBtn.on Events.Click, ->
	DNDBtn.visible = true
	ActiveBtn.visible = false
DNDBtn.on Events.Click, ->
	DNDBtn.visible = false
	ActiveBtn.visible = true
EncounterBtn.on Events.Click, ->
	NearbyView.visible = true
	NearbyView.sendToBack()
	ActiveView.animate
		properties:
			x: -3746

if notificationState
	NearbyView.visible = true
	
meeting = () ->
	if otherView
		MeetingView.visible = true
		MeetingView.sendToBack()
		NearbyView.animate
			properties:
				x: -374
		NudgeFromView.animate
			properties:
				x: -3746
	else
		ComingToYouView.visible = true
		ComingToYouView.sendToBack()
		ComingToYouBtn.superLayer = ComingToYouView
		ComeToMeBtn.superLayer = ComingToYouView
		NearbyView.animate
			properties:
				x: -3746
		NudgeFromView.animate
			properties:
				x: -3746

gotInvite = () ->
	if inviting
		meeting()
		return
	textNearby.visible = false
	invited = true
	text.html = 'Invite From'
	text.style.color = '#07C257'

document.onkeypress = gotInvite

Nudge.on Events.Click, ->
	nudge()
		
LetsMeet.on Events.Click, ->
	if invited
		meeting()
	else
		sendInvite()
		
ComingToYouBtn.on Events.Click, ->
	ComingToYouBtn.opacity = .5
	ComeToMeBtn.opacity = 1
ComeToMeBtn.on Events.Click, ->
	ComeToMeBtn.opacity = .5
	ComingToYouBtn.opacity = 1

sendInvite = () ->
	textNearby.visible = false
	text.html = 'Invite Sent'
	text.style.color = '#07C257'
	LetsMeet.visible = false
	Nudge.visible = false
	Nudge.centerX()
	inviting = true
	
nudge = () ->
	textNearby.visible = false
	text.html = 'Nudged'
	text.style.color = '#F5A623'
	Nudge.visible = false
	LetsMeet.bringToFront()
	LetsMeet.centerX()
	LetsMeet.visible = true

nudgedBy = () ->
	text.html = 'Nudge from'
	text.style.color = '#F5A623'

if otherView
	ActiveView.visible = false
	NudgeFromView.visible = true
	text.superLayer = NudgeFromView
	nudgedBy()
	Nudge.superLayer = NudgeFromView
	LetsMeet.superLayer = NudgeFromView

