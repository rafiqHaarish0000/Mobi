package com.mobiversa.ezy2pay.ui.receipt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.mobiversa.ezy2pay.R;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

public class ReceiptUtility {

    private static byte[] INIT = {0x1B, 0x40};
    private static byte[] POWER_ON = {0x1B, 0x3D, 0x01};
    private static byte[] POWER_OFF = {0x1B, 0x3D, 0x02};
    private static byte[] NEW_LINE = {0x0A};
    private static byte[] ALIGN_LEFT = {0x1B, 0x61, 0x00};
    private static byte[] ALIGN_CENTER = {0x1B, 0x61, 0x01};
    private static byte[] ALIGN_RIGHT = {0x1B, 0x61, 0x02};
    private static byte[] EMPHASIZE_ON = {0x1B, 0x45, 0x01};
    private static byte[] EMPHASIZE_OFF = {0x1B, 0x45, 0x00};
    private static byte[] FONT_5X8 = {0x1B, 0x4D, 0x00};
    private static byte[] FONT_5X12 = {0x1B, 0x4D, 0x01};
    private static byte[] FONT_8X12 = {0x1B, 0x4D, 0x02};
    private static byte[] FONT_10X18 = {0x1B, 0x4D, 0x03};
    private static byte[] FONT_SIZE_0 = {0x1D, 0x21, 0x00};
    private static byte[] FONT_SIZE_1 = {0x1D, 0x21, 0x11};
    private static byte[] CHAR_SPACING_0 = {0x1B, 0x20, 0x00};
    private static byte[] CHAR_SPACING_1 = {0x1B, 0x20, 0x01};

    private static byte[] hexToByteArray(String s) {
        if (s == null) {
            s = "";
        }
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        for (int i = 0; i < s.length() - 1; i += 2) {
            String data = s.substring(i, i + 2);
            bout.write(Integer.parseInt(data, 16));
        }
        return bout.toByteArray();
    }

    private static byte[] convertBitmap(Bitmap bitmap, int targetWidth, int threshold) {
        int targetHeight = (int) Math.round((double) targetWidth / (double) bitmap.getWidth() * (double) bitmap.getHeight());

        byte[] pixels = new byte[targetWidth * targetHeight];
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, false);
        for (int j = 0; j < scaledBitmap.getHeight(); ++j) {
            for (int i = 0; i < scaledBitmap.getWidth(); ++i) {
                int pixel = scaledBitmap.getPixel(i, j);
                int alpha = (pixel >> 24) & 0xFF;
                int r = (pixel >> 16) & 0xFF;
                int g = (pixel >> 8) & 0xFF;
                int b = pixel & 0xFF;
                if (alpha < 50) {
                    pixels[i + j * scaledBitmap.getWidth()] = 0;
                } else if ((r + g + b) / 3 >= threshold) {
                    pixels[i + j * scaledBitmap.getWidth()] = 0;
                } else {
                    pixels[i + j * scaledBitmap.getWidth()] = 1;
                }
            }
        }

        byte[] output = new byte[scaledBitmap.getWidth() * (int) Math.ceil((double) scaledBitmap.getHeight() / (double) 8)];

        for (int i = 0; i < scaledBitmap.getWidth(); ++i) {
            for (int j = 0; j < (int) Math.ceil((double) scaledBitmap.getHeight() / (double) 8); ++j) {
                for (int n = 0; n < 8; ++n) {
                    if (j * 8 + n < scaledBitmap.getHeight()) {
                        output[i + j * scaledBitmap.getWidth()] |= pixels[i + (j * 8 + n) * scaledBitmap.getWidth()] << (7 - n);
                    }
                }
            }
        }

        return output;
    }

    private static byte[] convertBarcode(Bitmap bitmap, int targetWidth, int threshold) {
        int targetHeight = (int) Math.round((double) targetWidth / (double) bitmap.getWidth() * (double) bitmap.getHeight());

        byte[] pixels = new byte[targetWidth];
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, false);
        for (int i = 0; i < scaledBitmap.getWidth(); ++i) {
            int pixel = scaledBitmap.getPixel(i, scaledBitmap.getHeight() / 2);
            int alpha = (pixel >> 24) & 0xFF;
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;
            if (alpha < 50) {
                pixels[i] = 0;
            } else if ((r + g + b) / 3 >= threshold) {
                pixels[i] = 0;
            } else {
                pixels[i] = 1;
            }
        }

        byte[] output = new byte[(int) Math.ceil((double) scaledBitmap.getWidth() / 8.0)];

        for (int i = 0; i < scaledBitmap.getWidth(); ++i) {
            output[i / 8] |= pixels[i] << (7 - (i % 8));
        }

        return output;
    }

    public static byte[] genReceipt(Context context, JSONObject receiptData, boolean isMerchantCopy) {
        try {
            JSONObject receipt = receiptData.has("responseData") ? receiptData.getJSONObject("responseData") : new JSONObject();

            int size0NoEmphasizeLineWidth = 384 / 8; //line width / font width
            StringBuilder singleLine = new StringBuilder();
            for (int i = 0; i < size0NoEmphasizeLineWidth / 2; ++i) {
                singleLine.append("-");
            }

            Bitmap logoBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.print_logo);
            int logoTargetWidth = 150;
            byte[] d1 = convertBitmap(logoBitmap, logoTargetWidth, 150);

            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                baos.write(INIT);
                baos.write(POWER_ON);
                baos.write(NEW_LINE);
                baos.write(ALIGN_CENTER);

                for (int j = 0; j < d1.length / logoTargetWidth; ++j) {
                    baos.write(hexToByteArray("1B2A00"));
                    baos.write((byte) logoTargetWidth);
                    baos.write((byte) (logoTargetWidth >> 8));
                    byte[] temp = new byte[logoTargetWidth];
                    System.arraycopy(d1, j * logoTargetWidth, temp, 0, temp.length);
                    baos.write(temp);
                    baos.write(NEW_LINE);
                }

                baos.write(EMPHASIZE_OFF);
                baos.write(FONT_10X18);
                baos.write(receipt.getString("merchantName").getBytes());
                baos.write(NEW_LINE);

                baos.write(FONT_SIZE_1);
                baos.write(FONT_5X12);
                baos.write(receipt.getString("merchantAddr").getBytes());
                baos.write(NEW_LINE);
                String cityZip = receipt.getString("merchantCity") + "," + receipt.getString("merchantPostCode");
                baos.write(cityZip.getBytes());
                baos.write(NEW_LINE);
                baos.write(receipt.getString("merchantPhone").getBytes());
                baos.write(NEW_LINE);

                baos.write(CHAR_SPACING_0);
                baos.write(singleLine.toString().getBytes());
                baos.write(NEW_LINE);

                baos.write(ALIGN_LEFT);
                baos.write(FONT_SIZE_1);
                baos.write(FONT_5X12);
                baos.write("TID: ".getBytes());
                baos.write(receipt.getString("tid").getBytes()); // replace tid
                baos.write(NEW_LINE);

                baos.write("MID: ".getBytes());
                baos.write(receipt.getString("mid").getBytes()); // replace mid
                baos.write(NEW_LINE);
                Double total = 0.00;
                try {
                    total = (Double.parseDouble(receipt.getString("amount")) + Double.parseDouble(receipt.getString("tips")));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                baos.write(ALIGN_CENTER);
                baos.write(FONT_10X18);
//				baos.write("SALE".getBytes());
                baos.write((receipt.has("txnType") ? receipt.getString("txnType") : "").getBytes());
                baos.write(NEW_LINE);
                baos.write(FONT_5X12);
                baos.write(ALIGN_LEFT);
                baos.write(EMPHASIZE_OFF);
                baos.write("DATE: ".getBytes());
                baos.write(receipt.getString("date").getBytes());
                baos.write(" TIME: ".getBytes());
                baos.write(receipt.getString("time").getBytes());

                baos.write(NEW_LINE);

                if (!(receipt.has("txnType") ? receipt.getString("txnType") : "").equalsIgnoreCase("CASH SALE")){
                    baos.write(FONT_5X12);
                    baos.write("Card No: ".getBytes());
                    baos.write((receipt.has("cardNo") ? receipt.getString("cardNo") : "").getBytes());
                    baos.write(NEW_LINE);

                    baos.write("NAME: ".getBytes());
                    baos.write((receipt.has("cardHolderName") ? receipt.getString("cardHolderName") : "").getBytes());
                    baos.write(NEW_LINE);

                    baos.write("App. : ".getBytes());
                    baos.write((receipt.has("approveCode") ? receipt.getString("approveCode") : "").getBytes());
                    baos.write(NEW_LINE);

                    baos.write("AID : ".getBytes());
                    baos.write((receipt.has("aid") ? receipt.getString("aid") : "").getBytes());
                    baos.write(NEW_LINE);

                    baos.write("TC : ".getBytes());
                    baos.write((receipt.has("tc") ? receipt.getString("tc") : "").getBytes());
                    baos.write(NEW_LINE);

                    baos.write("REF. NO: ".getBytes());
                    baos.write((receipt.has("rrn") ? receipt.getString("rrn") : "").getBytes());
                    baos.write(NEW_LINE);
//
                    baos.write("BATCH NO: ".getBytes());
                    baos.write((receipt.has("batchNo") ? receipt.getString("batchNo") : "").getBytes());
                    baos.write(NEW_LINE);
                }

                baos.write("Invoice : ".getBytes());
                baos.write((receipt.has("invoiceId") ? receipt.getString("invoiceId") : "").getBytes());
                baos.write(NEW_LINE);
                baos.write(NEW_LINE);

                baos.write("TRACE NO: ".getBytes());
                baos.write((receipt.has( "trace") ? receipt.getString("trace") : "").getBytes());
                baos.write(NEW_LINE);

                baos.write("AMOUNT: ".getBytes());
                baos.write(ALIGN_RIGHT);
                baos.write((receipt.has("amount") ? receipt.getString("amount") : "").getBytes());
                baos.write(NEW_LINE);

                baos.write("TIPS: ".getBytes());
                baos.write(ALIGN_RIGHT);
                baos.write((receipt.has("tips") ? receipt.getString("tips") : "").getBytes());
                baos.write(NEW_LINE);

                baos.write(EMPHASIZE_ON);
                baos.write(FONT_8X12);
                baos.write("TOTAL: ".getBytes());
                baos.write(ALIGN_RIGHT);
                baos.write(receipt.has("total") ? receipt.getString("total").getBytes() : String.format("%.2f", total).getBytes());

                baos.write(ALIGN_LEFT);
                baos.write(EMPHASIZE_OFF);
                baos.write(FONT_5X12);

                baos.write(NEW_LINE);
                baos.write("NO REFUND".getBytes());
                baos.write(NEW_LINE);


                if (!(receipt.has("txnType") ? receipt.getString("txnType") : "").equalsIgnoreCase("CASH SALE")){
                    baos.write(NEW_LINE);
                    baos.write(NEW_LINE);
                    baos.write(NEW_LINE);
                    baos.write(NEW_LINE);
                    baos.write("CARDHOLDER SIGNATURE".getBytes());
                }else {
                    baos.write("NO SIGNATURE REQUIRED".getBytes());
                }

                baos.write(NEW_LINE);
                baos.write(singleLine.toString().getBytes());
                baos.write(NEW_LINE);

                baos.write("I ACKNOWLEDGE SATISFACTORY RECEIPT OF RELATIVE GOODS/SERVICE".getBytes());
                baos.write(NEW_LINE);
                baos.write(NEW_LINE);

                baos.write(ALIGN_CENTER);
                String copyString = isMerchantCopy ? "**** MERCHANT COPY ****" : "**** CUSTOMER COPY ****";
                baos.write(copyString.getBytes());
                baos.write(NEW_LINE);
                baos.write(ALIGN_LEFT);

                baos.write("Power By : MOBIVERSA SDN BHD".getBytes());

                baos.write(NEW_LINE);
                baos.write(NEW_LINE);
                baos.write(NEW_LINE);
                baos.write(NEW_LINE);
                baos.write(NEW_LINE);
                baos.write(NEW_LINE);
                baos.write(POWER_OFF);
                return baos.toByteArray();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;//genDummyReceipt(context);
    }
}
