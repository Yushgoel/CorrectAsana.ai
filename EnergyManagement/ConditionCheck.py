import Categories

while True:
    for Categories.item in Categories.devices:
        Categories.onoff = (input("{}1=on, 0=off"))
        Categories.devices[Categories.item] = [Categories.watt, Categories.onoff, Categories.itemtype]

