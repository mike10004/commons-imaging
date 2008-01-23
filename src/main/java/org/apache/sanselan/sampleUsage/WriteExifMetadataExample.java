/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sanselan.sampleUsage;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.ImageWriteException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.common.RationalNumber;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;
import org.apache.sanselan.formats.tiff.TiffField;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.constants.TagInfo;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;
import org.apache.sanselan.formats.tiff.write.TiffOutputDirectory;
import org.apache.sanselan.formats.tiff.write.TiffOutputField;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;

public class WriteExifMetadataExample
{
	public void removeExifMetadata(File jpegImageFile, File dst)
			throws IOException, ImageReadException, ImageWriteException
	{
		OutputStream os = null;
		try
		{
			os = new FileOutputStream(dst);
			os = new BufferedOutputStream(os);

			new ExifRewriter().removeExifMetadata(jpegImageFile, os);
		}
		finally
		{
			if (os != null)
				try
				{
					os.close();
				}
				catch (IOException e)
				{

				}
		}
	}

	public void changeExifMetadata(File jpegImageFile, File dst)
			throws IOException, ImageReadException, ImageWriteException
	{
		OutputStream os = null;
		try
		{
			// note that metadata might be null if no metadata is found.
			IImageMetadata metadata = Sanselan.getMetadata(jpegImageFile);
			JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;

			// note that exif might be null if no Exif metadata is found.
			TiffImageMetadata exif = jpegMetadata.getExif();

			// TiffImageMetadata class is immutable (read-only).
			// TiffOutputSet class represents the Exif data to write.
			// 
			// Usually, we want to update existing Exif metadata by changing 
			// the values of a few fields, or adding a field.
			// In these cases, it is easiest to use getOutputSet() to 
			// start with a "copy" of the fields read from the image.
			TiffOutputSet outputSet = exif.getOutputSet();

			{
				// Example of how to remove a single tag/field.
				// 
				// Note that this approach is crude: Exif data is organized in 
				// directories.  The same tag/field may appear in more than one
				// directory.
				TiffOutputField aperture = outputSet
						.findField(TiffConstants.EXIF_TAG_APERTURE_VALUE);
				if (null != aperture)
				{
					// set contains aperture tag/field.
					outputSet
							.removeField(TiffConstants.EXIF_TAG_APERTURE_VALUE);
				}
			}

			{
				// Example of how to add a field/tag to the output set.
				//
				// Note that you should first remove the field/tag if it already exists 
				// in this directory.  See above.
				//
				// Certain fields/tags are expected in certain Exif directories;
				// Others can occur in more than one directory (and often have a different
				// meaning in different directories).
				//
				// TagInfo constants often contain a description of what directories
				// are associated with a given tag.
				// 
				// see org.apache.sanselan.formats.tiff.constants.AllTagConstants
				//
				TiffOutputField aperture = TiffOutputField.create(
						TiffConstants.EXIF_TAG_APERTURE_VALUE,
						outputSet.byteOrder, new Double(0.3));
				TiffOutputDirectory exifDirectory = outputSet
						.getOrCreateExifDirectory();
				exifDirectory.add(aperture);
			}

			//			printTagValue(jpegMetadata, TiffConstants.TIFF_TAG_DATE_TIME);

			os = new FileOutputStream(dst);
			os = new BufferedOutputStream(os);

			new ExifRewriter().updateExifMetadataLossless(jpegImageFile, os,
					outputSet);
		}
		finally
		{
			if (os != null)
				try
				{
					os.close();
				}
				catch (IOException e)
				{

				}
		}
	}

	public static void metadataExample(File file) throws ImageReadException,
			IOException
	{
		//        get all metadata stored in EXIF format (ie. from JPEG or TIFF).
		//            org.w3c.dom.Node node = Sanselan.getMetadataObsolete(imageBytes);
		IImageMetadata metadata = Sanselan.getMetadata(file);

		//System.out.println(metadata);

		if (metadata instanceof JpegImageMetadata)
		{
			JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;

			// Jpeg EXIF metadata is stored in a TIFF-based directory structure
			// and is identified with TIFF tags.
			// Here we look for the "x resolution" tag, but
			// we could just as easily search for any other tag.
			//
			// see the TiffConstants file for a list of TIFF tags.

			System.out.println("file: " + file.getPath());

			// print out various interesting EXIF tags.
			printTagValue(jpegMetadata, TiffConstants.TIFF_TAG_XRESOLUTION);
			printTagValue(jpegMetadata, TiffConstants.TIFF_TAG_DATE_TIME);
			printTagValue(jpegMetadata,
					TiffConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
			printTagValue(jpegMetadata, TiffConstants.EXIF_TAG_CREATE_DATE);
			printTagValue(jpegMetadata, TiffConstants.EXIF_TAG_ISO);
			printTagValue(jpegMetadata,
					TiffConstants.EXIF_TAG_SHUTTER_SPEED_VALUE);
			printTagValue(jpegMetadata, TiffConstants.EXIF_TAG_APERTURE_VALUE);
			printTagValue(jpegMetadata, TiffConstants.EXIF_TAG_BRIGHTNESS_VALUE);
			printTagValue(jpegMetadata, TiffConstants.GPS_TAG_GPS_LATITUDE_REF);
			printTagValue(jpegMetadata, TiffConstants.GPS_TAG_GPS_LATITUDE);
			printTagValue(jpegMetadata, TiffConstants.GPS_TAG_GPS_LONGITUDE_REF);
			printTagValue(jpegMetadata, TiffConstants.GPS_TAG_GPS_LONGITUDE);

			System.out.println();

			// more specific example of how to access GPS values.
			TiffField gpsLatitudeRefField = jpegMetadata
					.findEXIFValue(TiffConstants.GPS_TAG_GPS_LATITUDE_REF);
			TiffField gpsLatitudeField = jpegMetadata
					.findEXIFValue(TiffConstants.GPS_TAG_GPS_LATITUDE);
			TiffField gpsLongitudeRefField = jpegMetadata
					.findEXIFValue(TiffConstants.GPS_TAG_GPS_LONGITUDE_REF);
			TiffField gpsLongitudeField = jpegMetadata
					.findEXIFValue(TiffConstants.GPS_TAG_GPS_LONGITUDE);
			if (gpsLatitudeRefField != null && gpsLatitudeField != null
					&& gpsLongitudeRefField != null
					&& gpsLongitudeField != null)
			{
				// all of these values are strings.
				String gpsLatitudeRef = (String) gpsLatitudeRefField.getValue();
				RationalNumber gpsLatitude[] = (RationalNumber[]) (gpsLatitudeField
						.getValue());
				String gpsLongitudeRef = (String) gpsLongitudeRefField
						.getValue();
				RationalNumber gpsLongitude[] = (RationalNumber[]) gpsLongitudeField
						.getValue();

				RationalNumber gpsLatitudeDegrees = gpsLatitude[0];
				RationalNumber gpsLatitudeMinutes = gpsLatitude[1];
				RationalNumber gpsLatitudeSeconds = gpsLatitude[2];

				RationalNumber gpsLongitudeDegrees = gpsLongitude[0];
				RationalNumber gpsLongitudeMinutes = gpsLongitude[1];
				RationalNumber gpsLongitudeSeconds = gpsLongitude[2];

				// This will format the gps info like so:
				//
				// gpsLatitude: 8 degrees, 40 minutes, 42.2 seconds S
				// gpsLongitude: 115 degrees, 26 minutes, 21.8 seconds E

				System.out.println("	" + "GPS Latitude: "
						+ gpsLatitudeDegrees.toDisplayString() + " degrees, "
						+ gpsLatitudeMinutes.toDisplayString() + " minutes, "
						+ gpsLatitudeSeconds.toDisplayString() + " seconds "
						+ gpsLatitudeRef);
				System.out.println("	" + "GPS Longitude: "
						+ gpsLongitudeDegrees.toDisplayString() + " degrees, "
						+ gpsLongitudeMinutes.toDisplayString() + " minutes, "
						+ gpsLongitudeSeconds.toDisplayString() + " seconds "
						+ gpsLongitudeRef);

			}

			System.out.println();

			ArrayList items = jpegMetadata.getItems();
			for (int i = 0; i < items.size(); i++)
			{
				Object item = items.get(i);
				System.out.println("	" + "item: " + item);
			}

			System.out.println();
		}
	}

	private static void printTagValue(JpegImageMetadata jpegMetadata,
			TagInfo tagInfo) throws ImageReadException, IOException
	{
		TiffField field = jpegMetadata.findEXIFValue(tagInfo);
		if (field == null)
			System.out.println(tagInfo.name + ": " + "Not Found.");
		else
			System.out.println(tagInfo.name + ": "
					+ field.getValueDescription());
	}

}