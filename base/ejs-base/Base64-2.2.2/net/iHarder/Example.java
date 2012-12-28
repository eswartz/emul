package net.iHarder;

import java.io.IOException;


public class Example 
{
    
    public static void main(String[] args) 
    {
        // Make up some source objects
        javax.swing.JLabel originalLabel = new javax.swing.JLabel( "Base64 is great." );
        byte[] originalBytes = { (byte)-2, (byte)-1, (byte)0, (byte)1, (byte)2 };
        
        // Display original label
        System.out.println( "Original JLabel: " + originalLabel );
        
        // Encode serialized object
        String encLabel            = Base64.encodeObject( originalLabel );
        String encGZLabel          = Base64.encodeObject( originalLabel, Base64.GZIP );
        String encGZDontBreakLines = Base64.encodeObject( originalLabel, Base64.GZIP | Base64.DONT_BREAK_LINES );
        
        // Print encoded label
        System.out.println( "JLabel, encoded ( " + encLabel.getBytes().length + " bytes):\n" + encLabel );
        System.out.println( "JLabel, gzipped and encoded ( " + encGZLabel.getBytes().length + " bytes):\n" + encGZLabel );
        System.out.println( "JLabel, gzipped, encoded, no line breaks (not Base 64 compliant) ( " + encGZDontBreakLines.getBytes().length + " bytes):\n" + encGZDontBreakLines );
        
        // Decode label
        Object objLabel            = Base64.decodeToObject( encLabel );
        Object objGZLabel          = Base64.decodeToObject( encGZLabel );
        Object objGZDontBreakLines = Base64.decodeToObject( encGZDontBreakLines );
        
        // Display decoded label
        System.out.println( "Encoded JLabel -> decoded: " + objLabel );
        System.out.println( "Encoded, gzipped JLabel -> decoded: " + objGZLabel );
        System.out.println( "Encoded, gzipped, no line breaks JLabel -> decoded: " + objGZDontBreakLines );
        
        
        // Display original array
        System.out.println( "\n\nOriginal array: " );
        for( int i = 0; i < originalBytes.length; i++ )
            System.out.print( originalBytes[i] + " " );
        System.out.println();
        
        // Encode serialized bytes
        String encBytes            = Base64.encodeBytes( originalBytes );
        String encGZBytes          = Base64.encodeBytes( originalBytes, Base64.GZIP );
        
        // Print encoded bytes
        System.out.println( "Bytes, encoded ( " + encBytes.getBytes().length + " bytes):\n" + encBytes );
        System.out.println( "Bytes, gzipped and encoded ( " + encGZBytes.getBytes().length + " bytes):\n" + encGZBytes );
       
        // Decode bytes
        byte[] decBytes            = Base64.decode( encBytes );
        byte[] decGZBytes          = Base64.decode( encGZBytes );
        
        // Display decoded bytes
        System.out.println( "Encoded Bytes -> decoded: "  );
        for( int i = 0; i < decBytes.length; i++ )
            System.out.print( decBytes[i] + " " );
        System.out.println();
        System.out.println( "Encoded Bytes, gzipped -> decoded: "  );
        for( int i = 0; i < decGZBytes.length; i++ )
            System.out.print( decGZBytes[i] + " " );
        System.out.println();
        
        
        // Try suspend, resume
        // Base64 -> PrintStream -> System.out
        {
            System.out.println( "\n\nSuspend/Resume Base64.OutputStream" );
            Base64.OutputStream b64os = null;
            java.io.PrintStream ps    = null;

            try
            {
                ps    = new java.io.PrintStream( System.out );
                b64os = new Base64.OutputStream( ps, Base64.ENCODE );

                b64os.suspendEncoding();
                b64os.write( new String( "<mydata>" ).getBytes() );

                b64os.resumeEncoding();
                b64os.write( originalBytes );

                b64os.suspendEncoding();
                b64os.write( new String( "</mydata>\n\n" ).getBytes() );
            }   // end try
            catch( java.io.IOException e )
            {
                e.printStackTrace();
            }   // end catch
            finally
            {
                
            	if (b64os != null) {
            		try{ 
            			b64os.flush(); 
            			b64os.close();
            		} catch( IOException e ){}
            	}
                //try{ ps.close();    } catch( Exception e ){} // Closes System.out!
            }   // end finally
        }   // end suspsend/resume example
        
        
        // Encode something large to file, gzipped
        // ObjectOutput -> GZIP -> Base64 -> Buffer -> File
        {
            System.out.print( "\n\nWriting to file example.gz.txt..." );
            java.io.ObjectOutputStream     oos   = null;
            java.util.zip.GZIPOutputStream gzos  = null;
            Base64.OutputStream            b64os = null;
            java.io.BufferedOutputStream   bos   = null;
            java.io.FileOutputStream       fos   = null;

            try
            {
                fos   = new java.io.FileOutputStream( "example.gz.txt" );
                bos   = new java.io.BufferedOutputStream( fos );
                b64os = new Base64.OutputStream( bos, Base64.ENCODE );
                gzos  = new java.util.zip.GZIPOutputStream( b64os );
                oos   = new java.io.ObjectOutputStream( gzos );

                oos.writeObject( System.getProperties() );
            }   // end try
            catch( java.io.IOException e )
            {
                e.printStackTrace();
            }   // end catch
            finally
            {
                try{ oos.close();   } catch( Exception e ){}
                try{ gzos.close();  } catch( Exception e ){}
                try{ b64os.close(); } catch( Exception e ){}
                try{ bos.close();   } catch( Exception e ){}
                try{ fos.close();   } catch( Exception e ){}
                System.out.println( "Done." );
            }   // end finally
            
            // Read back in
            // File -> Buffer -> Base64 -> GZIP -> Object
            System.out.print( "\n\nReading from file example.gz.txt..." );
            java.io.ObjectInputStream     ois   = null;
            java.util.zip.GZIPInputStream gzis  = null;
            Base64.InputStream            b64is = null;
            java.io.BufferedInputStream   bis   = null;
            java.io.FileInputStream       fis   = null;

            try
            {
                fis   = new java.io.FileInputStream( "example.gz.txt" );
                bis   = new java.io.BufferedInputStream( fis );
                b64is = new Base64.InputStream( bis, Base64.DECODE );
                gzis  = new java.util.zip.GZIPInputStream( b64is );
                ois   = new java.io.ObjectInputStream( gzis );

                System.out.print( ois.readObject() );
            }   // end try
            catch( java.io.IOException e )
            {
                e.printStackTrace();
            }   // end catch
            catch( java.lang.ClassNotFoundException e )
            {
                e.printStackTrace();
            }   // end catch
            finally
            {
                try{ ois.close();   } catch( Exception e ){}
                try{ gzis.close();  } catch( Exception e ){}
                try{ b64is.close(); } catch( Exception e ){}
                try{ bis.close();   } catch( Exception e ){}
                try{ fis.close();   } catch( Exception e ){}
                System.out.println( "Done." );
            }   // end finally
        }   // end example: large to file, gzipped
        
        
        
        
        // Encode something large to file, NOT gzipped
        // ObjectOutput -> Base64 -> Buffer -> File
        {
            System.out.print( "\n\nWriting to file example.txt..." );
            java.io.ObjectOutputStream     oos   = null;
            Base64.OutputStream            b64os = null;
            java.io.BufferedOutputStream   bos   = null;
            java.io.FileOutputStream       fos   = null;

            try
            {
                fos   = new java.io.FileOutputStream( "example.txt" );
                bos   = new java.io.BufferedOutputStream( fos );
                b64os = new Base64.OutputStream( bos, Base64.ENCODE );
                oos   = new java.io.ObjectOutputStream( b64os );

                oos.writeObject( System.getProperties() );
            }   // end try
            catch( java.io.IOException e )
            {
                e.printStackTrace();
            }   // end catch
            finally
            {
                try{ oos.close();   } catch( Exception e ){}
                try{ b64os.close(); } catch( Exception e ){}
                try{ bos.close();   } catch( Exception e ){}
                try{ fos.close();   } catch( Exception e ){}
                System.out.println( "Done." );
            }   // end finally
        }   // end example: large to file, NOT gzipped
        
        
        
        
        
        System.out.println( "\nExamples completed." );
        
    }   // end main
    
}   // end class Example
