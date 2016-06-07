package net.KabOOm356.Service;

import net.KabOOm356.Service.SQLStatServices.ModeratorStatService;
import net.KabOOm356.Service.SQLStatServices.PlayerStatService;
import net.KabOOm356.Service.Store.StoreModule;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServiceModule {
	private static final Logger log = LogManager.getLogger(ServiceModule.class);

	private final StoreModule storeModule;
	private final PlayerMessageService playerMessageService;
	private final PlayerService playerService;
	private final LastViewedReportService lastViewedReportService;
	private final ReportCountService reportCountService;
	private final ReportValidatorService reportValidatorService;
	private final ReportInformationService reportInformationService;
	private final ReportPermissionService reportPermissionService;
	private final PermissionService permissionService;
	private final ReportLimitService limitService;
	private final ModeratorStatService modStatsService;
	private final PlayerStatService playerStatsService;

	public ServiceModule(final StoreModule storeModule) {
		if (log.isDebugEnabled()) {
			log.info("Initializing services...");
		}
		Validate.notNull(storeModule);
		this.storeModule = storeModule;
		permissionService = new PermissionService(this);
		playerService = new PlayerService(this);
		playerMessageService = new PlayerMessageService(this);
		lastViewedReportService = new LastViewedReportService(this);
		reportCountService = new ReportCountService(this);
		reportValidatorService = new ReportValidatorService(this);
		reportInformationService = new ReportInformationService(this);
		reportPermissionService = new ReportPermissionService(this);

		limitService = new ReportLimitService(this);
		modStatsService = new ModeratorStatService(this);
		playerStatsService = new PlayerStatService(this);
	}

	public ReportPermissionService getReportPermissionService() {
		return reportPermissionService;
	}

	public PlayerService getPlayerService() {
		return playerService;
	}

	public LastViewedReportService getLastViewedReportService() {
		return lastViewedReportService;
	}

	public ReportCountService getReportCountService() {
		return reportCountService;
	}

	public ReportValidatorService getReportValidatorService() {
		return reportValidatorService;
	}

	public ReportInformationService getReportInformationService() {
		return reportInformationService;
	}

	protected StoreModule getStore() {
		return storeModule;
	}

	public PermissionService getPermissionService() {
		return permissionService;
	}

	public PlayerMessageService getPlayerMessageService() {
		return playerMessageService;
	}

	public ReportLimitService getLimitService() {
		return limitService;
	}

	public ModeratorStatService getModStatsService() {
		return modStatsService;
	}

	public PlayerStatService getPlayerStatsService() {
		return playerStatsService;
	}
}
