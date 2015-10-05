import numpy as np
from scipy.fftpack import fft
import struct
import math
from PIL import Image, ImageDraw

f = open('/dev/shm/sig', 'rb')
fftSize = 1024


def myFFT(fh, fftSize, multiplier):
	samples = []

	for i in range(0, fftSize):
		a = fh.read(4)
		b = fh.read(4)
		samples.append(np.complex(struct.unpack('<f', b)[0], struct.unpack('<f', a)[0]))

	transformed = fft(samples, fftSize)

	mags = []
	for i in transformed:
		m = math.sqrt( (i.real * i.real) + (i.imag * i.imag) )

		if m == 0:
			mags.append(0.0)
		else:
			mags.append(abs(10 * math.log10(m)) * multiplier)
	
	return mags

imageHeight = 128
image = Image.new('RGB', (fftSize, imageHeight))
draw = ImageDraw.Draw(image)

avgs = []
for i in range(0, fftSize):
	avgs.append(0)

alpha = 0.01
color = (0, 255, 128, 0)

for a in range(0, 100):
	mags = myFFT(f, fftSize, 10)
	for i in range(0, len(mags)):
		avgs[i] = (avgs[i] * (1 - alpha)) + (alpha * mags[i])

for i in range(0, len(avgs) / 2):
	v = 0

	if int(avgs[i]) >= imageHeight - 1:
		v = 0
		print "Moo"
	else:
		v = int(avgs[i])
	
	draw.line(((fftSize / 2) + i, 0, (fftSize / 2) + i, v), color)

for i in range(fftSize/2, fftSize):
	v = 0
	if int(avgs[i]) >= imageHeight - 1:
		v = 0
		print "Moo"
	else:
		v = int(avgs[i])
	
	draw.line((  i - (fftSize/2), 0, i - (fftSize/2), v ), color)


draw.line((0, imageHeight-1, fftSize, imageHeight-1), (0, 255, 0, 0))


print "%f" % (avgs[fftSize-1])
print "%f" % (avgs[0])

image.save('/tmp/image.png')

#fftSize = 4096
#samples = []
#
#for i in range(0, fftSize):
#	samples.append(np.complex(struct.unpack('<f', f.read(4))[0], struct.unpack('<f', f.read(4))[0]))
#
#print "First raw sample: %s" % samples[0]
#
#transformed = fft(samples, fftSize)
#
#i = transformed[0]
#print "ABS of first FFT bin: %f" % abs(10 * math.log10(math.sqrt( (i.real * i.real) + (i.imag * i.imag) )) )
#
#a = 10 * np.log10(transformed)
#print "First mag bin: %s" % a[0]
#
#image = Image.new('RGB', (fftSize, 256))
#draw = ImageDraw.Draw(image)
#
#for i in range(0, len(transformed)):
#	s = transformed[i]
#	val = int(abs(10 * math.log10(math.sqrt( (s.real * s.real) + (s.imag * s.imag) )) ) * 10)
#	draw.line((i, 0, i, 256-val))
#	#print '=' * val
#
#image.save('/tmp/image.png')
