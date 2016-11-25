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
	private final ReportLimitService reportLimitService;
	private final ModeratorStatService modStatsService;
	private final PlayerStatService playerStatsService;
	private final ConfigurationService configurationService;

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
		configurationService = new ConfigurationService(this);

		reportLimitService = new ReportLimitService(this);
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

	public ReportLimitService getReportLimitService() {
		return reportLimitService;
	}

	public ModeratorStatService getModStatsService() {
		return modStatsService;
	}

	public PlayerStatService getPlayerStatsService() {
		return playerStatsService;
	}

	public ConfigurationService getConfigurationService() {
		return configurationService;
	}
}
