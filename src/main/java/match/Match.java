package match;

import java.util.List;

import mpicbg.spim.data.sequence.Channel;
import mpicbg.spim.data.sequence.ViewDescription;
import net.preibisch.mvrecon.fiji.spimdata.SpimData2;
import net.preibisch.mvrecon.process.interestpointregistration.pairwise.constellation.grouping.Group;

public class Match
{
	public static void main( String[] args )
	{
		final SpimData2 data = Util.openSpimData( "/Volumes/Samsung_T5/Laura/epi/dataset.xml" );
		final String label = "nuclei";

		final List< ViewDescription > confocal = 
				SpimData2.getAllViewIdsForChannelSorted( data, data.getSequenceDescription().getViewDescriptions().values(), new Channel( 0 ) );

		final List< ViewDescription > epi = 
				SpimData2.getAllViewIdsForChannelSorted( data, data.getSequenceDescription().getViewDescriptions().values(), new Channel( 1 ) );

		System.out.println( "Confocal:" );
		for ( final ViewDescription vd : confocal )
			System.out.println( Group.pvid( vd ) + ": " + Util.ips( data, vd, label ).size() + " nuclei." );

		System.out.println( "\nEpi:" );
		for ( final ViewDescription vd : epi )
			System.out.println( Group.pvid( vd ) + ": " + Util.ips( data, vd, label ).size() + " nuclei." );

		for ( final ViewDescription vdConfocal : confocal )
		{
			System.out.println( Group.pvid( vdConfocal ) );
			for ( final ViewDescription vdEpi : epi )
			{
				Util.findModel( data, vdConfocal, vdEpi, label, label );
			}
		}
	}
}
