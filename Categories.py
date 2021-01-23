import enum
start = None
end = None
next = None
watt = None
itemtype = None
item = None
people = 0
onoff = []
power = []
all = []

entertainment = []
cleaning = []
work = []
house = []
devices = []
kitchen = []

categories = {
    "ENTERTAINMENT" : 0,
    "CLEANING" : 1,
    "WORK" : 2,
    "HOUSE" : 3,
    "DEVICES": 4,
    "KITCHEN": 5
}

reverse_categories = {
    0: "ENTERTAINMENT",
    1: "CLEANING",
    2: "WORK",
    3: "HOUSE",
    4: "DEVICES",
    5: "KITCHEN"
}


condition = {}
wattage = {}

electronics = {}

start = int(input("Press 1 to start"))
#should be replace by a button

if start == 1:
    people = int(input("How many people are currently in your house?"))
    while end != 1:

        item = str(input("Item name: "))
        watt = str(input("Wattage: "))
        itemtype = int(input("0 for entertainment, 1 for cleaning, 2 for work, 3 for house, 4 for device, 5 for kitchen: "))

        electronics[item] = [watt, False, itemtype]

        end = int(input("Press 1 to end 0 to continue"))

        if end == 1:
            break
        else:
            pass
    print(electronics)
    #remove later only for testing

else:
    pass
    while next != 1:
        pass


