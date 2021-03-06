/*
 * Copyright (2010) Fondazione Bruno Kessler (FBK)
 * 
 * FBK reserves all rights in the Program as delivered.
 * The Program or any portion thereof may not be reproduced
 * in any form whatsoever except as provided by license
 * without the written consent of FBK.  A license under FBK's
 * rights in the Program may be available directly from FBK.
 */

package eu.fbk.utils.lsa.util;

import eu.fbk.utils.lsa.BOW;
import eu.fbk.utils.lsa.LSM;
import eu.fbk.utils.lsa.LSSimilarity;
import eu.fbk.utils.math.SparseVector;
import eu.fbk.utils.math.Vector;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.*;
import java.text.BreakIterator;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
public class Anvur {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>Anvur</code>.
     */
    static Logger logger = Logger.getLogger(Anvur.class.getName());

    //
    static List<String[]> readText(File f) throws IOException {
        logger.info("reading text: " + f + "...");
        List<String[]> list = new ArrayList<String[]>();
        //LineNumberReader lnr = new LineNumberReader(new FileReader(f));
        LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));

        String line = null;
        int count = 1;
        while ((line = lnr.readLine()) != null) {
            String[] s = StringEscapeUtils.unescapeHtml(line).split("\t");
            list.add(s);
        } // end while

        logger.info(list.size() + " lines read from in " + f);
        lnr.close();
        return list;
    } // end readText

    //
    static String tokenize(String in) {

        //print each word in order
        BreakIterator boundary = BreakIterator.getWordInstance();
        boundary.setText(in);
        StringBuilder out = new StringBuilder();
        int start = boundary.first();

        for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {
            out.append(" ");
            out.append(in.substring(start, end));
        }
        return out.toString();
    } // end tokenize

    //
    public static String buildName(String[] str, String f) {
        String[] s = f.split(",");
        int[] index = new int[s.length];
        for (int i = 0; i < s.length; i++) {
            index[i] = Integer.parseInt(s[i]);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < index.length; i++) {
            sb.append("-");
            sb.append(str[index[i]]);
        }
        return sb.toString();
    } // end buildName

    //
    public static Vector merge(Vector v1, Vector v2) {
        //v1.normalize();
        //v2.normalize();
        logger.info("v1\t" + v1);
        logger.info("v2\t" + v2);

        Vector m = new SparseVector();
        Iterator<Integer> it1 = v1.nonZeroElements();
        if (it1 != null) {
            while (it1.hasNext()) {
                int i = it1.next();
                m.add(i, v1.get(i));
            } // end while

        }

        Iterator<Integer> it2 = v2.nonZeroElements();
        if (it2 != null) {
            while (it2.hasNext()) {
                int i = it2.next();
                m.add(i + v1.size(), v2.get(i));
            } // end while

        }

        logger.info("merge\t" + v1.size() + "\t" + v2.size() + "\t" + m.size());
        logger.info(m);
        return m;
    } // end merge

    //
    public static String buildText(String[] str, String f) {
        String[] s = f.split(",");
        int[] index = new int[s.length];
        for (int i = 0; i < s.length; i++) {
            index[i] = Integer.parseInt(s[i]);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < index.length; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(str[index[i]]);

        }
        return sb.toString();
    } // end buildText

    //
    public static void main(String[] args) throws Exception {
        String logConfig = System.getProperty("log-config");
        if (logConfig == null) {
            logConfig = "log-config.txt";
        }

        PropertyConfigurator.configure(logConfig);
        /*
		 if (args.length != 2)
		 {
		 log.println("Usage: java -mx512M eu.fbk.utils.lsa.util.Anvur in-file out-dir");
		 System.exit(1);
		 }
		 
		 File l = new File(args[1]);
		 if (!l.exists())
		 {
		 l.mkdir();
		 }
		 List<String[]> list = readText(new File(args[0]));
		 String oldCategory = "";
		 for (int i=0;i<list.size();i++)
		 {
		 String[] s = list.get(i);
		 if (!oldCategory.equals(s[0]))
		 {
		 File f = new File(args[1] + File.separator + s[0]);
		 boolean b = f.mkdir();
		 logger.debug(f + " created " + b);
		 }
		 
		 File g = new File(args[1] + File.separator + s[0] + File.separator + s[1] + ".txt");
		 logger.debug("writing " + g + "...");
		 PrintWriter pw = new PrintWriter(new FileWriter(g));
		 //pw.println(tokenize(s[1].substring(0, s[1].indexOf(".")).replace('_', ' ') + " " + s[2]));
		 if (s.length == 5)
		 {
		 pw.println(tokenize(s[1].substring(0, s[1].indexOf(".")).replace('_', ' ') + " " + s[2] + " " + s[4].replace('_', ' ')));
		 }
		 else
		 {
		 pw.println(tokenize(s[1].substring(0, s[1].indexOf(".")).replace('_', ' ') + " " + s[2]));
		 }
		 pw.flush();
		 pw.close();
		 
		 } // end for i
		 */

        if (args.length != 7) {
            System.out.println(args.length);
            System.out.println(
                    "Usage: java -mx2G eu.fbk.utils.lsa.util.Anvur input threshold size dim idf in-file-csv fields\n\n");
            System.exit(1);
        }

        //
        DecimalFormat dec = new DecimalFormat("#.00");

        File Ut = new File(args[0] + "-Ut");
        File Sk = new File(args[0] + "-S");
        File r = new File(args[0] + "-row");
        File c = new File(args[0] + "-col");
        File df = new File(args[0] + "-df");
        double threshold = Double.parseDouble(args[1]);
        int size = Integer.parseInt(args[2]);
        int dim = Integer.parseInt(args[3]);
        boolean rescaleIdf = Boolean.parseBoolean(args[4]);

        //"author_check"0,	"authors"1,	"title"2,	"year"3,	"pubtype"4,	"publisher"5,	"journal"6,	"volume"7,	"number"8,	"pages"9,	"abstract"10,	"nauthors",	"citedby"
        String[] labels = {
                "author_check", "authors", "title", "year", "pubtype", "publisher", "journal", "volume", "number",
                "pages", "abstract", "nauthors", "citedby"
                //author_id	authors	title	year	pubtype	publisher	journal	volume	number	pages	abstract	nauthors	citedby

        };
        String name = buildName(labels, args[6]);

        File bwf = new File(args[5] + name + "-bow.txt");
        PrintWriter bw = new PrintWriter(
                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(bwf), "UTF-8")));
        File bdf = new File(args[5] + name + "-bow.csv");
        PrintWriter bd = new PrintWriter(
                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(bdf), "UTF-8")));
        File lwf = new File(args[5] + name + "-ls.txt");
        PrintWriter lw = new PrintWriter(
                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(lwf), "UTF-8")));
        File ldf = new File(args[5] + name + "-ls.csv");
        PrintWriter ld = new PrintWriter(
                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ldf), "UTF-8")));
        File blwf = new File(args[5] + name + "-bow+ls.txt");
        PrintWriter blw = new PrintWriter(
                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(blwf), "UTF-8")));
        File bldf = new File(args[5] + name + "-bow+ls.csv");
        PrintWriter bld = new PrintWriter(
                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(bldf), "UTF-8")));
        File logf = new File(args[5] + name + ".log");
        PrintWriter log = new PrintWriter(
                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logf), "UTF-8")));

        //System.exit(0);
        LSM lsm = new LSM(Ut, Sk, r, c, df, dim, rescaleIdf);
        LSSimilarity lss = new LSSimilarity(lsm, size);

        List<String[]> list = readText(new File(args[5]));

        // author_check	authors	title	year	pubtype	publisher	journal	volume	number	pages	abstract	nauthors	citedby

        //header
        for (int i = 0; i < list.size(); i++) {
            String[] s1 = list.get(i);
            String t1 = s1[0].toLowerCase();
            bw.print("\t");
            lw.print("\t");
            blw.print("\t");
            bw.print(i + "(" + s1[0] + ")");
            lw.print(i + "(" + s1[0] + ")");
            blw.print(i + "(" + s1[0] + ")");
        } // end for i

        bw.print("\n");
        lw.print("\n");
        blw.print("\n");
        for (int i = 0; i < list.size(); i++) {
            logger.info(i + "\t");
            String[] s1 = list.get(i);
            String t1 = buildText(s1, args[6]);
            BOW bow1 = new BOW(t1);
            logger.info(bow1);

            Vector d1 = lsm.mapDocument(bow1);
            d1.normalize();
            log.println("d1:" + d1);

            Vector pd1 = lsm.mapPseudoDocument(d1);
            pd1.normalize();
            log.println("pd1:" + pd1);

            Vector m1 = merge(pd1, d1);
            log.println("m1:" + m1);

            // write the orginal line
            for (int j = 0; j < s1.length; j++) {
                bd.print(s1[j]);
                bd.print("\t");
                ld.print(s1[j]);
                ld.print("\t");
                bld.print(s1[j]);
                bld.print("\t");

            }
            // write the bow, ls, and bow+ls vectors
            bd.println(d1);
            ld.println(pd1);
            bld.println(m1);

            bw.print(i + "(" + s1[0] + ")");
            lw.print(i + "(" + s1[0] + ")");
            blw.print(i + "(" + s1[0] + ")");
            for (int j = 0; j < i + 1; j++) {
                bw.print("\t");
                lw.print("\t");
                blw.print("\t");
            } // end for j

            for (int j = i + 1; j < list.size(); j++) {
                logger.info(i + "\t" + j);
                String[] s2 = list.get(j);

                String t2 = buildText(s2, args[6]);
                BOW bow2 = new BOW(t2);

                log.println(i + ":" + j + "(" + s1[0] + ":" + s2[0] + ") t1:" + t1);
                log.println(i + ":" + j + "(" + s1[0] + ":" + s2[0] + ") t2:" + t2);
                log.println(i + ":" + j + "(" + s1[0] + ":" + s2[0] + ") bow1:" + bow1);
                log.println(i + ":" + j + "(" + s1[0] + ":" + s2[0] + ") bow2:" + bow2);

                Vector d2 = lsm.mapDocument(bow2);
                d2.normalize();
                log.println("d2:" + d2);

                Vector pd2 = lsm.mapPseudoDocument(d2);
                pd2.normalize();
                log.println("pd2:" + pd2);

                Vector m2 = merge(pd2, d2);
                log.println("m2:" + m2);

                float cosVSM = d1.dotProduct(d2) / (float) Math.sqrt(d1.dotProduct(d1) * d2.dotProduct(d2));
                float cosLSM = pd1.dotProduct(pd2) / (float) Math.sqrt(pd1.dotProduct(pd1) * pd2.dotProduct(pd2));
                float cosBOWLSM = m1.dotProduct(m2) / (float) Math.sqrt(m1.dotProduct(m1) * m2.dotProduct(m2));
                bw.print("\t");
                bw.print(dec.format(cosVSM));
                lw.print("\t");
                lw.print(dec.format(cosLSM));
                blw.print("\t");
                blw.print(dec.format(cosBOWLSM));

                log.println(i + ":" + j + "(" + s1[0] + ":" + s2[0] + ") bow\t" + cosVSM);
                log.println(i + ":" + j + "(" + s1[0] + ":" + s2[0] + ") ls:\t" + cosLSM);
                log.println(i + ":" + j + "(" + s1[0] + ":" + s2[0] + ") bow+ls:\t" + cosBOWLSM);
            }
            bw.print("\n");
            lw.print("\n");
            blw.print("\n");
        } // end for i

        logger.info("wrote " + bwf);
        logger.info("wrote " + bwf);
        logger.info("wrote " + bdf);
        logger.info("wrote " + lwf);
        logger.info("wrote " + ldf);
        logger.info("wrote " + blwf);
        logger.info("wrote " + bldf);
        logger.info("wrote " + logf);

        ld.close();
        bd.close();
        bld.close();
        bw.close();
        lw.close();
        blw.close();

        log.close();

    } // end main

} // end Anvur
