import re

#with open("users", "r") as a_file:
#      for line in a_file:
#              stripped_line = line.strip()
#              splitted = re.split(r'\t+', stripped_line)
#              print('("'+splitted[0]+'","'+splitted[1]+'","'+splitted[2]+'",'+splitted[3]+","+splitted[4]+',"'+splitted[5]+'"),')
with open("teams", "r") as a_file:
      for line in a_file:
              stripped_line = line.strip()
              splitted = re.split(r'\t+', stripped_line)
              print('("'+splitted[0]+'",'+splitted[1]+'),')
