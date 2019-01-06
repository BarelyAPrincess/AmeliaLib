/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.users;

import io.amelia.foundation.EntityPrincipal;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.ReportingLevel;

/**
 * EntityPrincipal Exception Container
 */
public class UserException
{
	public UserException()
	{
		// Static Access
	}

	public static class Error extends ApplicationException.Error
	{
		private static final long serialVersionUID = 5522301956671473324L;
		private final DescriptiveReason descriptiveReason;
		private final EntityPrincipal entityPrincipal;

		public Error( EntityPrincipal entityPrincipal )
		{
			super();
			this.entityPrincipal = entityPrincipal;
			this.descriptiveReason = null;
		}

		public Error( EntityPrincipal entityPrincipal, String message )
		{
			super( message );
			this.entityPrincipal = entityPrincipal;
			this.descriptiveReason = null;
		}

		public Error( EntityPrincipal entityPrincipal, String message, Throwable cause )
		{
			super( message, cause );
			this.entityPrincipal = entityPrincipal;
			this.descriptiveReason = null;
		}

		public Error( EntityPrincipal entityPrincipal, Throwable cause )
		{
			super( cause );
			this.entityPrincipal = entityPrincipal;
			this.descriptiveReason = null;
		}

		public Error( EntityPrincipal entityPrincipal, ReportingLevel level )
		{
			super( level );
			this.entityPrincipal = entityPrincipal;
			this.descriptiveReason = null;
		}

		public Error( EntityPrincipal entityPrincipal, ReportingLevel level, String message )
		{
			super( level, message );
			this.entityPrincipal = entityPrincipal;
			this.descriptiveReason = null;
		}

		public Error( EntityPrincipal entityPrincipal, ReportingLevel level, String message, Throwable cause )
		{
			super( level, message, cause );
			this.entityPrincipal = entityPrincipal;
			this.descriptiveReason = null;
		}

		public Error( EntityPrincipal entityPrincipal, ReportingLevel level, Throwable cause )
		{
			super( level, cause );
			this.entityPrincipal = entityPrincipal;
			this.descriptiveReason = null;
		}

		public Error( EntityPrincipal entityPrincipal, DescriptiveReason descriptiveReason, Throwable cause )
		{
			super( descriptiveReason.getReportingLevel(), descriptiveReason.getReasonMessage(), cause );
			this.entityPrincipal = entityPrincipal;
			this.descriptiveReason = descriptiveReason;
		}

		public Error( EntityPrincipal entityPrincipal, DescriptiveReason descriptiveReason )
		{
			super( descriptiveReason.getReportingLevel(), descriptiveReason.getReasonMessage() );
			this.entityPrincipal = entityPrincipal;
			this.descriptiveReason = descriptiveReason;
		}

		public EntityPrincipal getEntityPrincipal()
		{
			return entityPrincipal;
		}

		public DescriptiveReason getDescriptiveReason()
		{
			return descriptiveReason;
		}
	}

	public static class Runtime extends ApplicationException.Runtime
	{
		private static final long serialVersionUID = 5522301956671473324L;

		public Runtime()
		{
			super();
		}

		public Runtime( String message )
		{
			super( message );
		}

		public Runtime( String message, Throwable cause )
		{
			super( message, cause );
		}

		public Runtime( Throwable cause )
		{
			super( cause );
		}
	}
}
