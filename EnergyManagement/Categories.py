import enum
import ConditionCheck

start = None
end = None
next = None
watt = None
itemtype = None
item = None
quit = None
people = 0
onoff = []
power = []
all = []

entertainment = []
cleaning = []
work = []
house = []
electronics = []
kitchen = []

categories = {
    "ENTERTAINMENT" : 0,
    "CLEANING" : 1,
    "WORK" : 2,
    "HOUSE" : 3,
    "ELECTRONICS": 4,
    "KITCHEN": 5
}

reverse_categories = {
    0: "ENTERTAINMENT",
    1: "CLEANING",
    2: "WORK",
    3: "HOUSE",
    4: "ELECTRONICS",
    5: "KITCHEN"
}




condition = {}
wattage = {}

devices = {}

start = int(input("Press 1 to start"))
#should be replaced by a button

if start == 1:
    #people = int(input("How many people are currently in your house?"))
    while end != 1:

        item = str(input("Item name: "))
        watt = int(input("Wattage: "))
        #itemtype = int(input("0 for entertainment, 1 for cleaning, 2 for work, 3 for house, 4 for electronics, 5 for kitchen: "))
        itemtype = 0
        #onoff = int(input("Press 1 if it is on and 0 if it is off"))
        onoff = 1

        devices[item] = [watt, onoff, itemtype]

        end = int(input("Press 1 to end 0 to continue"))

        if end == 1:
            break
        else:
            pass

    #remove later only for testing


    print("Your")
    for item in devices:
        if devices[item][0] > 1000:
          print("{},".format(item))
        else:
            pass


    print("are consuming a lot of power, considering switching them off when unnecessary")

quit = int(input("Press 1 to stop running"))
start = 0



