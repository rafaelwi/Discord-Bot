"""
Gift Image Generator by rafaelwi
    For use with the gift command.

    A script that generates images for the embedded gift message based on the 
    avatars of the giver and reciever. To use this script, call the following:
        python3 GiftImgGenerator.py GIVER_IMG_URL RECIEVE_IMG_URL
    
    The URLs can be retrieved with the User.getAvatarUrl() method in JDA. This
    script will take care of the downloading and removing of the user avatars,
    however the bot will need to take care of removing the image produced by 
    this script.
"""
from PIL import Image, ImageDraw
import requests
import sys

default_img_path = '../../../../res/defaultGift.png'
giver_img_path = '../../../../res/' + sys.argv[1].split('/')[-1]
recieve_img_path = '../../../../res/' + sys.argv[2].split('/')[-1]
giftbox_img_path = '../../../../res/giftbox.png'

# Get the images given from stdin. The script expects the giver's image URL
# to be given first and the reciever's image URL to be given after. If for 
# whatever reason, the downloading of the two images fail, then this will
# return the location of the default image. PLEASE DO NOT DELETE THAT IMAGE!
if len(sys.argv) < 3:
    print("[GIMGG Error] Not enough parameters passed in. Usage: " +
        "python3 GiftImgGenerator.py GIVER_IMG_URL RECIEVE_IMG_URL")
    sys.stdout.write(default_img_path)
    sys.exit(0)

# Download the first image, timeout is set to 500ms
giver_img_request = requests.get(sys.argv[1], timeout=0.5)
if giver_img_request.status_code != 200:
    sys.stdout.write(default_img_path)
    sys.exit(0)

giver_img_file = open(giver_img_path, 'wb')
giver_img_file.write(giver_img_request.content)
giver_img_file.close()

# Download the second image, timeout is set to 500ms
recieve_img_request = requests.get(sys.argv[2], timeout=0.5)
if recieve_img_request.status_code != 200:
    sys.stdout.write(default_img_path)
    sys.exit(0)
recieve_img_file = open(recieve_img_path, 'wb')
recieve_img_file.write(recieve_img_request.content)
recieve_img_file.close()

# Now take the two images and combine them together using PIL
gift_img = Image.new('RGBA', (300, 300), (255, 0, 0, 0))
giver_img = Image.open(giver_img_path).resize((150, 150))
recieve_img = Image.open(recieve_img_path).resize((150, 150))
giftbox_img = Image.open(giftbox_img_path). resize((100, 100))

# Create a circular mask for the giver and receiver
mask = Image.new('L', (150, 150), 0)
draw = ImageDraw.Draw(mask)
draw.ellipse((0, 0) + (150, 150), fill=255)

# Paste the three images together
gift_img.paste(giver_img, (0, 0), mask)
gift_img.paste(recieve_img, (150, 150), mask)
gift_img.paste(giftbox_img, (100, 100), giftbox_img)

# Save the image and return its filename. It is the responsibility of the 
# caller to delete the image once it it done with it.
gift_img.save('../../../../res/giftimg.png', 'PNG')
sys.stdout.write('../../../../res/giftimg.png')
