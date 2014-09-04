/*
  RtLPCEngine.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.speech.encode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**

 * @author ejs
 *
 */
public class RtLPCEngine implements ILPCEngine {
	private float[] corr;
	private float[] Zs;
	private Matrix R;
	private Matrix res;
	private int order;
	private LPCEncoderParams params;
	
	public RtLPCEngine(LPCEncoderParams params) {
		this.params = params;
		this.order = params.getOrder();
		
    	R = new Matrix( order, order );
        res = new Matrix( order, order );
        Zs = new float[order];
		this.Zss = new float[order];

	    // allocate
		corr = new float[params.getFrameSize()];
	    
	}
	
	private float predict(float[] x, int offs, int len, LPCAnalysisFrame results) {
		int i,j;
	    float power = 0.0f;
	    float error, tmp;
	    for(  i = 0; i < order; i++ )
	        Zs[i] = x[order - i - 1 + offs];

	    // set the hope size
	    int hope_size = len - order;
	    
	    // zero out the residue
	    if( results.residue != null ) 
	    	Arrays.fill(results.residue, 0);

	    // find the MSE
	    for( i = order; i < hope_size + order; i++ )
	    {
	        tmp = 0.0f;
	        for( j = 0; j < order; j++ ) tmp += Zs[j] * results.coefs[j];
	        for( j = order - 1; j > 0; j-- ) Zs[j] = Zs[j-1];
	        Zs[0] = x[i+offs];
	        error = x[i+offs] - tmp;
	        power += error * error;
	        
	        if( results.residue != null ) results.residue[i] = error;
	    }

	    // power
	    return (float)Math.sqrt(power) / hope_size;
	}

	@Override
	public float[] getY() {
		return corr;
	}
	//-----------------------------------------------------------------------------
	// name: lpc_analyze()
	// desc: ...
	//-----------------------------------------------------------------------------
	/* (non-Javadoc)
	 * @see v9t9.audio.speech.encode.ILPCAnalysis#analyze(float[], int, int, v9t9.audio.speech.encode.LPCAnalysis.LPCAnalysisFrame)
	 */
	@Override
	public LPCAnalysisFrame analyze( float[] x, int offs, int len)
	{
	    int i, j;
	    
	    LPCAnalysisFrame results = new LPCAnalysisFrame(order);
	    results.coefsOffs = 1;
	    
	    if (this.corr == null || this.corr.length != len) {
	    	corr = new float[len];
	    }
	    
	    // find the autocorrelation of the signal, with pitch
	    results.invPitch = autocorrelate( x, offs, len, corr );
	    results.pitch = results.invPitch != 0 ? (results.invPitch * params.getPlaybackHz() / params.getHertz() ) : 0;
	    
	    // construct the R matrix
	    for( i = 0; i < order; i++ )
	        for( j = 0; j < order; j++ )
	             R.set(i, j, corr[Math.abs((int)(i-j))]);

	    // invert R
	    R.invert( res );

	    // find the coefficients A = P*R^(-1)
	    results.coefs = new float[order + 1];
	    for( i = 0; i < order; i++ )
	    {
	        results.coefs[i] = 0.0f;
	        for( j = 0; j < order; j++ )
	        	results.coefs[i] += R.at(i, j) * corr[1+j];
	    }

	    // do the linear prediction to find residue
	    results.power = predict( x, offs, len, results) ;
	    results.powerScale = params.getFrameSize() ;
	    
	    return results;
	}
	

	static int numGraphs;
	static StringBuilder graphData = new StringBuilder();
	static boolean showGraph = false;
	
	//-----------------------------------------------------------------------------
	// name: autocorrelate()
	// desc: ...
	//-----------------------------------------------------------------------------
	private float autocorrelate( float[] x, int offs, int len, float[]  y )
	{
	    float norm, temp;
	    int n, i, j, k;

	    // refer to pp. 89 for variable name consistency
	    for ( n = 0; n < len; n++ )
	    {
	        temp = 0.0f;
	        for ( i = 0; i < len - offs - n - 1; i++ )
	            temp += x[i + offs] * x[i+offs+n];
	        y[n] = temp;
	        
	        if (showGraph) {
	        	graphData.append(n + " " + y[n] + "\n");
	        	
	        }
	    }
	    
	    if (showGraph) {
	    	graphData.append("\n");
	    	numGraphs++;
	    	if (numGraphs == 63) {
	    		try {
					File tmp = File.createTempFile("grf", ".txt");
					OutputStream os = new FileOutputStream(tmp);
					os.write(graphData.toString().getBytes());
					os.close();
					
					Runtime.getRuntime().exec("xgraph " + tmp);
					tmp.deleteOnExit();
				} catch (IOException e) {
					e.printStackTrace();
				}
	    		graphData.setLength(0);
	    		numGraphs = 0;
	    	}
	    }
	    

	    // set temp to the first element of y
	    temp = y[0];
	    // why?
	    j = (int)(len * 0.02);
	    // loop to the point y stops descreasing
	    while( y[j] < temp && j < len )
	    {
	        temp = y[j];
	        j++;
	    }

	    // yes
	    temp = 0.0f;
	    // find the max between j and the end
	    for( i = j; i < len/2; i++ )
	    {
	        if( y[i] > temp)
	        {
	            j = i;
	            temp = y[i];
	        }
	    }

	    // detect voiced vs. unvoiced -- 0 means unvoiced
	    norm = 1.0f / len;
	    k = len;

	    for( i = 0; i < len; i++ )
	        y[i] *= (k-i) * norm;

	    if( (y[j] / y[0]) < 0.4 ) j = 0;
	    if( j > len / 2 ) j = 0;

	    // we return the pitch information
	    return (float) j;
	}


	private int ticker;
	private boolean alt;
	private float[] Zss;



	//-----------------------------------------------------------------------------
	// name: lpc_synthesize()
	// desc: ...
	//-----------------------------------------------------------------------------
	public void synthesize( float[] y, int offs, int len, int playbackHz, LPCAnalysisFrame frame)
	{
	    float output;
	    int i, j;
	    
	    float invPitch = frame.invPitch * playbackHz / params.getHertz();
	    
	    for( i = 0; i < len; i++ ) {
	        output = 0.0f;

			if( invPitch == 0 )
	        {
	            output = (float) (frame.power * 20 * ( 2.0f * Math.random() - 1.0f ));
	            ticker = 0;
	            if( i == (len - 1) || i == 0 )
	            	Arrays.fill(Zss, 0);
	        }
	        else {
	            ticker--;
	            if( ticker <= 0 ) {
	                ticker = (int)(invPitch + .5f);
	                if( !alt ) {
	                	output = frame.power * invPitch * 1.0f;
	                    //output = frame.power;
	                }
	            }

	            if( alt  )
	            {
	                j = (int)(invPitch+.5) - ticker + 0;
	                if( j >= 0 && (j*4) < glot_pop_data.length ) {
	                    output = frame.power * invPitch * 0.5f * glot_pop_data[j*4] / (float)32767;
	                    //output = frame.power * glot_pop_data[j*4] / (float)32767;
	                }
	                else output *= .9f;
	            }
	        }

	        for( j = 0; j < frame.coefs.length; j++ )
	            output += Zss[j] * frame.coefs[j];

	        for( j = frame.coefs.length - 1; j > 0; j-- )
	            Zss[j] = Zss[j-1];

	        Zss[0] = output;

	        y[i + offs] = output;
	    }
	}



	// data for glot_pop.raw...
	static float glot_pop_data[] = {
	    0.0f,0.0f,1.0f,-1.0f,0.0f,0.0f,0.0f,0.0f,
	    1.0f,1.0f,513.0f,561.0f,565.0f,499.0f,374.0f,188.0f,
	    -42.0f,-306.0f,-571.0f,-821.0f,-1037.0f,-1214.0f,-1338.0f,-1422.0f,
	    -1479.0f,-1520.0f,-1568.0f,-1628.0f,-1708.0f,-1814.0f,-1931.0f,-2062.0f,
	    -2185.0f,-2293.0f,-2375.0f,-2432.0f,-2464.0f,-2476.0f,-2471.0f,-2458.0f,
	    -2431.0f,-2397.0f,-2338.0f,-2247.0f,-2110.0f,-1916.0f,-1655.0f,-1330.0f,
	    -950.0f,-528.0f,-91.0f,337.0f,730.0f,1059.0f,1310.0f,1467.0f,
	    1531.0f,1509.0f,1411.0f,1256.0f,1066.0f,847.0f,629.0f,415.0f,
	    217.0f,37.0f,-131.0f,-278.0f,-411.0f,-524.0f,-624.0f,-711.0f,
	    -778.0f,-827.0f,-859.0f,-867.0f,-866.0f,-846.0f,-824.0f,-804.0f,
	    -788.0f,-789.0f,-808.0f,-855.0f,-926.0f,-1019.0f,-1142.0f,-1282.0f,
	    -1441.0f,-1617.0f,-1803.0f,-2007.0f,-2223.0f,-2446.0f,-2670.0f,-2886.0f,
	    -3077.0f,-3235.0f,-3330.0f,-3347.0f,-3278.0f,-3107.0f,-2844.0f,-2499.0f,
	    -2102.0f,-1674.0f,-1253.0f,-869.0f,-548.0f,-299.0f,-131.0f,-29.0f,
	    24.0f,59.0f,94.0f,150.0f,240.0f,368.0f,519.0f,688.0f,
	    856.0f,994.0f,1093.0f,1144.0f,1135.0f,1077.0f,968.0f,823.0f,
	    646.0f,458.0f,268.0f,82.0f,-93.0f,-255.0f,-395.0f,-523.0f,
	    -641.0f,-749.0f,-858.0f,-979.0f,-1114.0f,-1266.0f,-1437.0f,-1610.0f,
	    -1779.0f,-1925.0f,-2028.0f,-2073.0f,-2047.0f,-1953.0f,-1795.0f,-1592.0f,
	    -1374.0f,-1167.0f,-995.0f,-888.0f,-851.0f,-891.0f,-983.0f,-1112.0f,
	    -1239.0f,-1325.0f,-1345.0f,-1266.0f,-1081.0f,-788.0f,-412.0f,21.0f,
	    465.0f,881.0f,1223.0f,1460.0f,1576.0f,1556.0f,1423.0f,1201.0f,
	    934.0f,660.0f,430.0f,280.0f,234.0f,300.0f,459.0f,685.0f,
	    930.0f,1143.0f,1275.0f,1285.0f,1145.0f,866.0f,465.0f,-8.0f,
	    -490.0f,-913.0f,-1208.0f,-1322.0f,-1233.0f,-941.0f,-472.0f,96.0f,
	    701.0f,1240.0f,1641.0f,1848.0f,1830.0f,1590.0f,1181.0f,678.0f,
	    174.0f,-238.0f,-481.0f,-502.0f,-293.0f,120.0f,687.0f,1322.0f,
	    1943.0f,2470.0f,2853.0f,3075.0f,3151.0f,3120.0f,3036.0f,2957.0f,
	    2929.0f,2977.0f,3082.0f,3206.0f,3299.0f,3295.0f,3153.0f,2863.0f,
	    2431.0f,1916.0f,1405.0f,999.0f,799.0f,891.0f,1299.0f,2024.0f,
	    2997.0f,4118.0f,5278.0f,6312.0f,7105.0f,7554.0f,7625.0f,7328.0f,
	    6724.0f,5925.0f,5063.0f,4280.0f,3700.0f,3391.0f,3411.0f,3737.0f,
	    4307.0f,5027.0f,5773.0f,6436.0f,6918.0f,7164.0f,7178.0f,7003.0f,
	    6733.0f,6509.0f,6468.0f,6734.0f,7386.0f,8444.0f,9841.0f,11430.0f,
	    12991.0f,14273.0f,15014.0f,14983.0f,14047.0f,12167.0f,9434.0f,6071.0f,
	    2379.0f,-1263.0f,-4504.0f,-7035.0f,-8657.0f,-9297.0f,-9036.0f,-8060.0f,
	    -6653.0f,-5126.0f,-3761.0f,-2782.0f,-2285.0f,-2276.0f,-2655.0f,-3251.0f,
	    -3867.0f,-4324.0f,-4512.0f,-4379.0f,-3970.0f,-3372.0f,-2719.0f,-2129.0f,
	    -1704.0f,-1489.0f,-1470.0f,-1589.0f,-1747.0f,-1853.0f,-1844.0f,-1693.0f,
	    -1423.0f,-1108.0f,-819.0f,-663.0f,-707.0f,-966.0f,-1411.0f,-1953.0f,
	    -2478.0f,-2836.0f,-2913.0f,-2625.0f,-1947.0f,-922.0f,362.0f,1760.0f,
	    3116.0f,4278.0f,5127.0f,5586.0f,5646.0f,5340.0f,4750.0f,3974.0f,
	    3115.0f,2269.0f,1495.0f,822.0f,254.0f,-237.0f,-678.0f,-1090.0f,
	    -1487.0f,-1856.0f,-2172.0f,-2406.0f,-2515.0f,-2473.0f,-2289.0f,-1978.0f,
	    -1593.0f,-1201.0f,-876.0f,-681.0f,-661.0f,-836.0f,-1184.0f,-1664.0f,
	    -2198.0f,-2717.0f,-3148.0f,-3432.0f,-3544.0f,-3495.0f,-3322.0f,-3073.0f,
	    -2829.0f,-2648.0f,-2581.0f,-2666.0f,-2885.0f,-3213.0f,-3593.0f,-3957.0f,
	    -4233.0f,-4365.0f,-4312.0f,-4064.0f,-3636.0f,-3057.0f,-2378.0f,-1656.0f,
	    -945.0f,-289.0f,262.0f,697.0f,1002.0f,1167.0f,1205.0f,1115.0f,
	    917.0f,630.0f,272.0f,-130.0f,-548.0f,-946.0f,-1294.0f,-1572.0f,
	    -1753.0f,-1837.0f,-1835.0f,-1763.0f,-1642.0f,-1510.0f,-1391.0f,-1303.0f,
	    -1259.0f,-1251.0f,-1264.0f,-1281.0f,-1271.0f,-1229.0f,-1139.0f,-1000.0f,
	    -835.0f,-669.0f,-530.0f,-446.0f,-440.0f,-517.0f,-678.0f,-905.0f,
	    -1181.0f,-1471.0f,-1750.0f,-1999.0f,-2197.0f,-2333.0f,-2411.0f,-2422.0f,
	    -2375.0f,-2259.0f,-2090.0f,-1850.0f,-1543.0f,-1169.0f,-737.0f,-260.0f,
	    232.0f,707.0f,1122.0f,1444.0f,1639.0f,1688.0f,1586.0f,1347.0f,
	    1010.0f,615.0f,212.0f,-153.0f,-440.0f,-635.0f,-733.0f,-759.0f,
	    -745.0f,-744.0f,-793.0f,-930.0f,-1170.0f,-1513.0f,-1938.0f,-2401.0f,
	    -2860.0f,-3257.0f,-3553.0f,-3720.0f,-3747.0f,-3643.0f,-3430.0f,-3141.0f,
	    -2811.0f,-2480.0f,-2170.0f,-1903.0f,-1676.0f,-1497.0f,-1356.0f,-1241.0f,
	    -1151.0f,-1083.0f,-1041.0f,-1016.0f,-1017.0f,-1041.0f,-1081.0f,-1127.0f,
	    -1165.0f,-1182.0f,-1164.0f,-1110.0f,-1007.0f,-871.0f,-706.0f,-533.0f,
	    -361.0f,-211.0f,-89.0f,-4.0f,51.0f,68.0f,63.0f,48.0f,
	    24.0f,5.0f,-12.0f,-22.0f,-26.0f,-23.0f,-17.0f,-10.0f,
	    -5.0f,2.0f,4.0f,6.0f,6.0f,6.0f,5.0f,0.0f,
	    0.0f,
	    0
	}; 

}
