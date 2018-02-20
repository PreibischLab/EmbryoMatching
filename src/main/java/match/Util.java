package match;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mpicbg.models.AffineModel3D;
import mpicbg.models.InterpolatedAffineModel3D;
import mpicbg.models.Model;
import mpicbg.models.RigidModel3D;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.sequence.Channel;
import mpicbg.spim.data.sequence.ViewId;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import net.preibisch.mvrecon.fiji.spimdata.SpimData2;
import net.preibisch.mvrecon.fiji.spimdata.XmlIoSpimData2;
import net.preibisch.mvrecon.fiji.spimdata.interestpoints.InterestPoint;
import net.preibisch.mvrecon.fiji.spimdata.interestpoints.InterestPointList;
import net.preibisch.mvrecon.fiji.spimdata.interestpoints.ViewInterestPointLists;
import net.preibisch.mvrecon.process.interestpointregistration.TransformationTools;
import net.preibisch.mvrecon.process.interestpointregistration.pairwise.MatcherPairwise;
import net.preibisch.mvrecon.process.interestpointregistration.pairwise.MatcherPairwiseTools;
import net.preibisch.mvrecon.process.interestpointregistration.pairwise.PairwiseResult;
import net.preibisch.mvrecon.process.interestpointregistration.pairwise.constellation.grouping.Group;
import net.preibisch.mvrecon.process.interestpointregistration.pairwise.methods.ransac.RANSACParameters;
import net.preibisch.mvrecon.process.interestpointregistration.pairwise.methods.rgldm.RGLDMPairwise;
import net.preibisch.mvrecon.process.interestpointregistration.pairwise.methods.rgldm.RGLDMParameters;

public class Util
{
	public static void findModel( final SpimData2 spimData, final ViewId viewIdA, final ViewId viewIdB, final String labelA, final String labelB )
	{
		final Map< ViewId, String > labelMap = new HashMap<>();

		labelMap.put( viewIdA, labelA );
		labelMap.put( viewIdB, labelB );

		// load & transform all interest points
		final Map< ViewId, List< InterestPoint > > interestpoints =
				TransformationTools.getAllTransformedInterestPoints(
					labelMap.keySet(),
					spimData.getViewRegistrations().getViewRegistrations(),
					spimData.getViewInterestPoints().getViewInterestPoints(),
					labelMap );

		//System.out.println( interestpoints.get( viewIdA ).size() );
		//System.out.println( interestpoints.get( viewIdB ).size() );

		final List< Pair< ViewId, ViewId > > pairs = new ArrayList<>();

		pairs.add( new ValuePair< ViewId, ViewId >( viewIdA, viewIdB ) );

		final Model< ? > model = new InterpolatedAffineModel3D< AffineModel3D, RigidModel3D >( new AffineModel3D(), new RigidModel3D(), 0.1f );
		final RGLDMParameters params = new RGLDMParameters( model, Float.MAX_VALUE, 1.5f, 3, 3 );
		final RANSACParameters ransac = new RANSACParameters( 5, RANSACParameters.min_inlier_ratio / 10, 2f, 100000 );
		final RGLDMPairwise< InterestPoint > matcher = new RGLDMPairwise<>( ransac, params );

		matcher.setPrintResult( false );

		// compute all pairwise matchings
		final List< Pair< Pair< ViewId, ViewId >, PairwiseResult< InterestPoint > > > result =
				MatcherPairwiseTools.computePairs( pairs, interestpoints, matcher );

		if ( result.get( 0 ).getB().getInliers().size() > 0 )
			System.out.println( result.get( 0 ).getB().getFullDesc() );
	}

	public static List< InterestPoint > ips( final SpimData2 spimData, final ViewId viewId, final String label )
	{
		final ViewInterestPointLists vipl = spimData.getViewInterestPoints().getViewInterestPointLists( viewId );
		final InterestPointList vip = vipl.getInterestPointList( label );

		if ( vip == null )
			return new ArrayList<>();
		else
			return vip.getInterestPointsCopy();
	}

	public static SpimData2 openSpimData( final String xml )
	{
		try
		{
			final SpimData2 spimData = new XmlIoSpimData2( "" ).load( xml );

			final List< Channel > channels = SpimData2.getAllChannelsSorted( spimData, spimData.getSequenceDescription().getViewDescriptions().values() );

			if ( channels.size() != 2 )
			{
				System.out.println( "We expect two channels, there are: " + channels.size() );
 	 			return null;
			}

			return spimData;
		}
		catch ( SpimDataException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
}
