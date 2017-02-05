#!python3

import os, sqlite3, shutil, re

conn = sqlite3.connect("fr_tb_dic.db")
cursor = conn.cursor()
q = "SELECT _id, letter FROM sound_corr WHERE _id > 29;"
res_dic = {}
for row in cursor.execute(q).fetchall():
	res_dic[row[0]] = row[1]


if not os.path.exists('temp_ogg_files'):
	os.makedirs('temp_ogg_files')


cursor2 = conn.cursor()
q2 = "UPDATE sound_corr SET sound=? WHERE _id=?"

regex = re.compile(r"\(.*\)", re.IGNORECASE)

for key, letter_to_pronounce in res_dic.items():
	
	filename = 'temp_ogg_files/'+ str(key) + '.wav'
	os.system('ekho -v \'Tibetan\' \"' + letter_to_pronounce + 
				'\" -t wav -o \'' + filename + '\' -s -30')
	print(str(key) + ' <---> ' + str(letter_to_pronounce))
	with open(filename, 'rb') as f:
		ablob = f.read()
	
	cursor2.execute(q2, [sqlite3.Binary(ablob), key,])

conn.commit()

conn.close()

#  Cleaning after processed
if os.path.exists('temp_ogg_files'):
	shutil.rmtree('temp_ogg_files', ignore_errors=True)

